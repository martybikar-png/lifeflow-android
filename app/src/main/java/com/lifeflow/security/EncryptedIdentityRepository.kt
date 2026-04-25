package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.security.DomainOperation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
                operation = DomainOperation.SAVE_IDENTITY,
                reason = "save(identity) requires active auth session"
            )

            val plain = serialize(identity)

            try {
                encryptedIdentityWriteVersionedBlob(
                    id = identity.id,
                    plain = plain,
                    operation = "save()",
                    blobStore = blobStore,
                    encryptionService = encryptionService,
                    vault = vault
                )
            } catch (t: Throwable) {
                SecurityKeystoreFailureHandler.throwForFailure(
                    operation = DomainOperation.SAVE_IDENTITY,
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
                operation = DomainOperation.READ_IDENTITY_BY_ID,
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
                SecurityKeystoreFailureHandler.throwForFailure(
                    operation = DomainOperation.READ_IDENTITY_BY_ID,
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
                operation = DomainOperation.READ_ACTIVE_IDENTITY,
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
                SecurityKeystoreFailureHandler.throwForFailure(
                    operation = DomainOperation.READ_ACTIVE_IDENTITY,
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
                operation = DomainOperation.DELETE_IDENTITY,
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
                SecurityKeystoreFailureHandler.throwForFailure(
                    operation = DomainOperation.DELETE_IDENTITY,
                    failureReason = "delete failed for id=${identity.id}",
                    genericMessage = "EncryptedIdentityRepository: delete() failure",
                    throwable = t
                )
            }
        }
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
