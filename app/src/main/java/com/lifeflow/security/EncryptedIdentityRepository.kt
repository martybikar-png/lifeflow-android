package com.lifeflow.security

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.charset.StandardCharsets
import java.util.UUID

class EncryptedIdentityRepository(
    private val delegate: IdentityRepository,
    private val encryptionService: EncryptionService
) : IdentityRepository {

    private val mutex = Mutex()

    /**
     * Encrypted-at-rest storage inside this repository.
     * Stores: id -> (IV + ciphertext) produced by EncryptionService.encrypt()
     */
    private val encryptedStore: MutableMap<UUID, ByteArray> = mutableMapOf()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            // 🔐 B: hard gate (must be within recent biometric session window)
            SecurityAccessSession.requireAuthorized("save(identity) requires active auth session")

            val plain = serialize(identity)
            // AAD binds ciphertext to identity id (prevents swapping ciphertexts across ids)
            val aad = identity.id.toString().toByteArray(StandardCharsets.UTF_8)

            val cipher = encryptionService.encrypt(plain, aad)
            encryptedStore[identity.id] = cipher

            // Optional: keep delegate updated for compatibility/migration (non-authoritative)
            delegate.save(identity)
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            val cipher = encryptedStore[id]
            if (cipher != null) {
                // 🔐 B: hard gate
                SecurityAccessSession.requireAuthorized("getById(id) requires active auth session")

                val aad = id.toString().toByteArray(StandardCharsets.UTF_8)
                val plain = encryptionService.decrypt(cipher, aad)
                return@withLock deserialize(plain)
            }

            // Fallback if something exists only in the delegate (pre-encryption state)
            delegate.getById(id)
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            if (encryptedStore.isNotEmpty()) {
                // 🔐 B: hard gate (decrypt loop)
                SecurityAccessSession.requireAuthorized("getActiveIdentity() requires active auth session")
            }

            // Prefer encrypted store (authoritative)
            for ((id, cipher) in encryptedStore) {
                val aad = id.toString().toByteArray(StandardCharsets.UTF_8)
                val plain = encryptionService.decrypt(cipher, aad)
                val identity = deserialize(plain)
                if (identity.isActive) return@withLock identity
            }

            // Fallback (pre-encryption state)
            delegate.getActiveIdentity()
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            // 🔐 B: treat delete as sensitive (mutating protected storage)
            SecurityAccessSession.requireAuthorized("delete(identity) requires active auth session")

            encryptedStore.remove(identity.id)
            delegate.delete(identity)
        }
    }

    private fun serialize(identity: LifeFlowIdentity): ByteArray {
        // Stable, minimal serialization (no Android deps)
        // Format: uuid|createdAtEpochMillis|isActive
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