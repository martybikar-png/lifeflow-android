package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.charset.StandardCharsets
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
                SecurityRuleEngine.reportCryptoFailure(
                    action = RuleAction.WRITE_SAVE,
                    reason = "save failed for id=${identity.id}",
                    throwable = t
                )
                throw SecurityException("EncryptedIdentityRepository: save() crypto failure", t)
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
                SecurityRuleEngine.reportCryptoFailure(
                    action = RuleAction.READ_BY_ID,
                    reason = "decrypt/deserialize failed for id=$id",
                    throwable = t
                )
                throw SecurityException("EncryptedIdentityRepository: getById() integrity failure", t)
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
                SecurityRuleEngine.reportCryptoFailure(
                    action = RuleAction.READ_ACTIVE,
                    reason = "decrypt/deserialize failed during scan",
                    throwable = t
                )
                throw SecurityException("EncryptedIdentityRepository: getActiveIdentity() integrity failure", t)
            }
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.WRITE_DELETE,
                reason = "delete(identity) requires active auth session"
            )

            try {
                blobStore.delete(identity.id)
                vault.clearIdentityVersion(identity.id)
            } catch (t: Throwable) {
                SecurityRuleEngine.reportCryptoFailure(
                    action = RuleAction.WRITE_DELETE,
                    reason = "delete failed for id=${identity.id}",
                    throwable = t
                )
                throw SecurityException("EncryptedIdentityRepository: delete() failure", t)
            }
        }
    }

    /**
     * Strict decryption:
     * - Reads explicit scheme marker if present
     * - NO downgrade fallback
     * - If blob is versioned, requires version > 0
     * - If blob is legacy (or unmarked legacy), uses legacy AAD
     * - Read paths stay read-only; no migration side effects here
     */
    private fun decryptStrict(id: UUID, version: Long, stored: ByteArray): ByteArray {
        val (scheme, cipher) = unwrapSchemeMarker(stored)

        return when (scheme) {
            SCHEME_VERSIONED -> {
                require(version > 0L) { "Missing vault version for versioned blob id=$id" }
                encryptionService.decryptVersionedFormat(cipher, aadVersioned(id, version))
            }

            SCHEME_LEGACY -> {
                encryptionService.decryptLegacyFormat(cipher, aadLegacy(id))
            }

            else -> throw SecurityException("Unknown blob scheme marker for id=$id")
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
        val aad = aadVersioned(id, nextVersion)

        val stored = try {
            val cipher = encryptionService.encrypt(plain, aad)
            wrapWithSchemeMarker(SCHEME_VERSIONED, cipher)
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

    private fun restorePreviousBlob(id: UUID, previousStored: ByteArray?, original: Throwable) {
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

    private fun aadLegacy(id: UUID): ByteArray =
        id.toString().toByteArray(StandardCharsets.UTF_8)

    private fun aadVersioned(id: UUID, version: Long): ByteArray =
        "${id}|v$version".toByteArray(StandardCharsets.UTF_8)

    private fun wrapWithSchemeMarker(marker: Byte, cipher: ByteArray): ByteArray {
        val out = ByteArray(1 + cipher.size)
        out[0] = marker
        System.arraycopy(cipher, 0, out, 1, cipher.size)
        return out
    }

    private fun unwrapSchemeMarker(stored: ByteArray): Pair<Byte, ByteArray> {
        if (stored.isEmpty()) throw IllegalArgumentException("Empty stored blob")

        val first = stored[0]
        return when (first) {
            SCHEME_VERSIONED, SCHEME_LEGACY -> {
                val cipher = stored.copyOfRange(1, stored.size)
                first to cipher
            }

            else -> {
                SCHEME_LEGACY to stored
            }
        }
    }

    private fun serialize(identity: LifeFlowIdentity): ByteArray {
        val s = buildString {
            append(identity.id.toString())
            append("|")
            append(identity.createdAtEpochMillis)
            append("|")
            append(identity.isActive)
        }
        return s.toByteArray(StandardCharsets.UTF_8)
    }

    private fun deserialize(bytes: ByteArray): LifeFlowIdentity {
        val s = String(bytes, StandardCharsets.UTF_8)
        val parts = s.split("|")
        require(parts.size == 3) { "Invalid identity payload" }

        val id = UUID.fromString(parts[0])
        val createdAt = parts[1].toLong()
        val isActive = parts[2].toBooleanStrictOrNull()
            ?: throw IllegalArgumentException("Invalid identity active flag")

        return LifeFlowIdentity(
            id = id,
            createdAtEpochMillis = createdAt,
            isActive = isActive
        )
    }

    private companion object {
        private const val MARKER_VERSIONED: Int = 0xA1
        private const val MARKER_LEGACY: Int = 0xA2

        private val SCHEME_VERSIONED: Byte = MARKER_VERSIONED.toByte()
        private val SCHEME_LEGACY: Byte = MARKER_LEGACY.toByte()
    }
}