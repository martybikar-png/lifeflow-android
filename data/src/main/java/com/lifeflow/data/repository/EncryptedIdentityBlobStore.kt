package com.lifeflow.data.repository

import android.content.Context
import android.util.Base64
import java.util.UUID

/**
 * V1 persistent ciphertext store.
 * Stores: UUID -> Base64( IV + ciphertext )
 *
 * NOTE:
 * - This store is "dumb": it doesn't know anything about identities.
 * - Security/authorization is enforced in the security layer (EncryptedIdentityRepository + RuleEngine).
 */
class EncryptedIdentityBlobStore(
    context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun put(id: UUID, blob: ByteArray) {
        val key = keyFor(id)
        val encoded = Base64.encodeToString(blob, Base64.NO_WRAP)
        val currentIndex = prefs.getStringSet(KEY_INDEX, emptySet())?.toSet() ?: emptySet()

        val ok = prefs.edit()
            .putString(key, encoded)
            .putStringSet(KEY_INDEX, currentIndex + key)
            .commit()

        if (!ok) throw IllegalStateException("BlobStore put commit failed for id=$id")
    }

    fun get(id: UUID): ByteArray? {
        val key = keyFor(id)
        val encoded = prefs.getString(key, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    fun delete(id: UUID) {
        val key = keyFor(id)
        val currentIndex = prefs.getStringSet(KEY_INDEX, emptySet())?.toSet() ?: emptySet()

        val ok = prefs.edit()
            .remove(key)
            .putStringSet(KEY_INDEX, currentIndex - key)
            .commit()

        if (!ok) throw IllegalStateException("BlobStore delete commit failed for id=$id")
    }

    fun entries(): List<Pair<UUID, ByteArray>> {
        val keys = prefs.getStringSet(KEY_INDEX, emptySet())?.toSet() ?: emptySet()
        return keys.mapNotNull { k ->
            val encoded = prefs.getString(k, null) ?: return@mapNotNull null
            val blob = Base64.decode(encoded, Base64.NO_WRAP)
            val uuid = uuidFromKey(k) ?: return@mapNotNull null
            uuid to blob
        }
    }

    /**
     * Deterministic wipe:
     * - removes all stored ciphertext blobs + index
     * - uses commit() so reset flow can be fail-closed and reliable
     */
    fun clearAll() {
        val keys = prefs.getStringSet(KEY_INDEX, emptySet())?.toSet() ?: emptySet()
        val editor = prefs.edit()
        keys.forEach { editor.remove(it) }
        editor.remove(KEY_INDEX)

        val ok = editor.commit()
        if (!ok) throw IllegalStateException("BlobStore clearAll commit failed")
    }

    private fun keyFor(id: UUID): String = "id_$id"

    private fun uuidFromKey(key: String): UUID? {
        if (!key.startsWith("id_")) return null
        return runCatching { UUID.fromString(key.removePrefix("id_")) }.getOrNull()
    }

    private companion object {
        private const val PREFS_NAME = "lifeflow_identity_cipher_store_v1"
        private const val KEY_INDEX = "cipher_keys_index"
    }
}