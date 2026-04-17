package com.lifeflow.security

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException
import java.util.UUID

/**
 * Authoritative encrypted-at-rest identity repository.
 *
 * Phase D (hardened):
 * - Rollback/downgrade mitigation via:
 *   1) Monotonic version binding in Vault
 *   2) Explicit blob scheme marker (legacy vs versioned)
 *   3) Fail-closed: NO "try versioned then fallback to legacy"
 *
 * Backward-compatibility:
 * - Old stored blobs had NO scheme marker and used legacy AAD = "$UUID"
 * - We treat "no marker" as legacy and allow decrypt only in legacy mode
 * - Legacy blobs are NOT auto-migrated during read paths
 * - Migration to versioned storage happens only on explicit write/save paths
 *
 * Consistency hardening:
 * - Encrypt for the next version first
 * - Write blob first
 * - Commit vault version second
 * - If vault version commit fails, roll blob back best-effort and fail closed
 */
class EncryptedIdentityRepository(
    private val blobStore: EncryptedIdentityBlobStore,
    private val encryptionService: EncryptionService,
    private val vault: AndroidDataSovereigntyVault
) : IdentityRepository {

    private companion object {
        private const val MARKER_VERSIONED: Int = 0xA1
        private const val MARKER_LEGACY: Int = 0xA2

        private val SCHEME_VERSIONED: Byte = MARKER_VERSIONED.toByte()
        private val SCHEME_LEGACY: Byte = MARKER_LEGACY.toByte()
    }

    private val mutex = Mutex()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.WRITE_SAVE,
                reason = "save(identity) requires active auth session"
            )

            val plain = serialize(identity)

            try {
                writeVersionedBlob(
                    id = identity.id,
                    plain = plain,
                    operation = "save()"
                )
            } catch (t: Throwable) {
                handleProtectedStorageFailure(
                    action = RuleAction.WRITE_SAVE,
                    failureReason = "save failed for id=${identity.id}",
                    genericMessage = "EncryptedIdentityRepository: save() crypto failure",
                    throwable = t
                )
            }
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.READ_BY_ID,
                reason = "getById(id) requires active auth session"
            )

            val stored = blobStore.get(id) ?: return@withLock null
            val version = vault.getIdentityVersion(id)

            try {
                val plain = decryptStrict(id, version, stored)
                val identity = deserialize(plain)
                require(identity.id == id) { "Identity id mismatch for requested id=$id" }
                identity
            } catch (t: Throwable) {
                handleProtectedStorageFailure(
                    action = RuleAction.READ_BY_ID,
                    failureReason = "decrypt/deserialize failed for id=$id",
                    genericMessage = "EncryptedIdentityRepository: getById() integrity failure",
                    throwable = t
                )
            }
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.READ_ACTIVE,
                reason = "getActiveIdentity() requires active auth session"
            )

            try {
                for ((id, stored) in blobStore.entries()) {
                    val version = vault.getIdentityVersion(id)
                    val plain = decryptStrict(id, version, stored)
                    val identity = deserialize(plain)

                    if (identity.id != id) {
                        throw SecurityException("Identity id mismatch during active scan for id=$id")
                    }

                    if (identity.isActive) return@withLock identity
                }
                null
            } catch (t: Throwable) {
                handleProtectedStorageFailure(
                    action = RuleAction.READ_ACTIVE,
                    failureReason = "decrypt/deserialize failed during scan",
                    genericMessage = "EncryptedIdentityRepository: getActiveIdentity() integrity failure",
                    throwable = t
                )
            }
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.WRITE_DELETE,
                reason = "delete(identity) requires active auth session"
            )

            val stored = blobStore.get(identity.id) ?: return@withLock
            val version = vault.getIdentityVersion(identity.id)

            try {
                val plain = decryptStrict(identity.id, version, stored)
                val storedIdentity = deserialize(plain)
                require(storedIdentity.id == identity.id) {
                    "Identity id mismatch for delete id=${identity.id}"
                }

                blobStore.delete(identity.id)
                vault.clearIdentityVersion(identity.id)
            } catch (t: Throwable) {
                handleProtectedStorageFailure(
                    action = RuleAction.WRITE_DELETE,
                    failureReason = "delete failed for id=${identity.id}",
                    genericMessage = "EncryptedIdentityRepository: delete() failure",
                    throwable = t
                )
            }
        }
    }

    /**
     * Writes a versioned blob with best-effort consistency:
     * 1) compute next version without mutating vault
     * 2) encrypt for that next version
     * 3) write blob
     * 4) commit vault version
     * 5) if step 4 fails, restore previous blob and fail closed
     *
     * NOTE:
     * - This helper does not report to SecurityRuleEngine directly
     * - The authoritative caller reports once at the boundary to avoid duplicate audit noise
     */
    private fun writeVersionedBlob(
        id: UUID,
        plain: ByteArray,
        operation: String
    ) {
        val previousVersion = vault.getIdentityVersion(id)
        val nextVersion = previousVersion + 1L
        require(nextVersion > 0L) { "Version overflow for id=$id" }

        val previousStored = blobStore.get(id)

        val stored = try {
            encryptedIdentityEncryptVersionedBlob(
                id = id,
                version = nextVersion,
                plain = plain,
                encryptionService = encryptionService
            )
        } catch (t: Throwable) {
            throw SecurityException(
                "EncryptedIdentityRepository: $operation encrypt failed for id=$id",
                t
            )
        }

        try {
            blobStore.put(id, stored)

            val committedVersion = vault.nextIdentityVersion(id)
            check(committedVersion == nextVersion) {
                "Vault version mismatch for id=$id: expected=$nextVersion actual=$committedVersion"
            }
        } catch (t: Throwable) {
            restorePreviousBlob(id, previousStored, t)
            throw SecurityException(
                "EncryptedIdentityRepository: $operation consistency failure for id=$id",
                t
            )
        }
    }

    private fun restorePreviousBlob(
        id: UUID,
        previousStored: ByteArray?,
        original: Throwable
    ) {
        runCatching {
            if (previousStored == null) {
                blobStore.delete(id)
            } else {
                blobStore.put(id, previousStored)
            }
        }.onFailure { rollbackFailure ->
            original.addSuppressed(rollbackFailure)
        }
    }

    private fun handleProtectedStorageFailure(
        action: RuleAction,
        failureReason: String,
        genericMessage: String,
        throwable: Throwable
    ): Nothing {
        when {
            hasCause<UserNotAuthenticatedException>(throwable) -> {
                SecurityAccessSession.clear()
                throw SecurityException(
                    "$genericMessage. Recent biometric authentication is required.",
                    throwable
                )
            }

            hasCause<KeyPermanentlyInvalidatedException>(throwable) ||
                hasCause<UnrecoverableKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_RECOVERY_REQUIRED: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore key was invalidated. Reset vault is required.",
                    throwable
                )
            }

            hasCause<SecurityKeystorePostureException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_POSTURE_VIOLATION: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore security posture is not valid. Reset vault is required.",
                    throwable
                )
            }

            hasCause<InvalidKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_INVALID_KEY: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore key is not operational. Reset vault is required.",
                    throwable
                )
            }

            else -> {
                SecurityRuleEngine.reportCryptoFailure(
                    action = action,
                    reason = failureReason,
                    throwable = throwable
                )
                throw SecurityException(genericMessage, throwable)
            }
        }
    }

    private inline fun <reified T : Throwable> hasCause(
        throwable: Throwable
    ): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }
        return false
    }

    // Compatibility bridge for existing reflection-based unit tests.
    private fun decryptStrict(
        id: UUID,
        version: Long,
        stored: ByteArray
    ): ByteArray {
        return encryptedIdentityDecryptStrict(
            id = id,
            version = version,
            stored = stored,
            encryptionService = encryptionService
        )
    }

    private fun aadLegacy(id: UUID): ByteArray {
        return encryptedIdentityAadLegacyForTests(id)
    }

    private fun aadVersioned(
        id: UUID,
        version: Long
    ): ByteArray {
        return encryptedIdentityAadVersionedForTests(id, version)
    }

    private fun wrapWithSchemeMarker(
        marker: Byte,
        cipher: ByteArray
    ): ByteArray {
        return encryptedIdentityWrapWithSchemeMarkerForTests(marker, cipher)
    }

    private fun unwrapSchemeMarker(stored: ByteArray): Pair<Byte, ByteArray> {
        return encryptedIdentityUnwrapSchemeMarkerForTests(stored)
    }

    private fun serialize(identity: LifeFlowIdentity): ByteArray {
        return encryptedIdentitySerialize(identity)
    }

    private fun deserialize(bytes: ByteArray): LifeFlowIdentity {
        return encryptedIdentityDeserialize(bytes)
    }
}
