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
 * Storage:
 * - Persisted ciphertext blobs (IV + ciphertext) in EncryptedIdentityBlobStore
 *
 * Enforcement:
 * - Phase III RuleEngine (V1): denied-by-default, requires active SecurityAccessSession
 *
 * Crypto:
 * - AAD binds ciphertext to identity UUID to prevent ciphertext swapping across ids
 */
class EncryptedIdentityRepository(
    private val blobStore: EncryptedIdentityBlobStore,
    private val encryptionService: EncryptionService
) : IdentityRepository {

    private val mutex = Mutex()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.WRITE_SAVE,
                reason = "save(identity) requires active auth session"
            )

            val plain = serialize(identity)
            val aad = identity.id.toString().toByteArray(StandardCharsets.UTF_8)

            val cipher = encryptionService.encrypt(plain, aad)
            blobStore.put(identity.id, cipher)
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.READ_BY_ID,
                reason = "getById(id) requires active auth session"
            )

            val cipher = blobStore.get(id) ?: return@withLock null
            val aad = id.toString().toByteArray(StandardCharsets.UTF_8)

            val plain = encryptionService.decrypt(cipher, aad)
            deserialize(plain)
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.READ_ACTIVE,
                reason = "getActiveIdentity() requires active auth session"
            )

            for ((id, cipher) in blobStore.entries()) {
                val aad = id.toString().toByteArray(StandardCharsets.UTF_8)
                val plain = encryptionService.decrypt(cipher, aad)
                val identity = deserialize(plain)
                if (identity.isActive) return@withLock identity
            }
            null
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            SecurityRuleEngine.requireAllowed(
                action = RuleAction.WRITE_DELETE,
                reason = "delete(identity) requires active auth session"
            )
            blobStore.delete(identity.id)
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
        val isActive = parts[2].toBooleanStrictOrNull() ?: false

        return LifeFlowIdentity(
            id = id,
            createdAtEpochMillis = createdAt,
            isActive = isActive
        )
    }
}