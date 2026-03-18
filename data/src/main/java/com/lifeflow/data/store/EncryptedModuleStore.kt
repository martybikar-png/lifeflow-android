package com.lifeflow.data.store

import android.content.Context
import android.util.Base64
import com.lifeflow.domain.core.EncryptionPort

/**
 * EncryptedModuleStore — generic encrypted key-value store for module data.
 *
 * Uses EncryptionPort (AES-256-GCM via domain boundary) for all writes.
 * GCM tag provides built-in anti-tampering — decrypt fails on any modification.
 * Fail-closed on decrypt errors — returns null.
 * Fail-closed on write errors — throws.
 *
 * Each module uses its own prefsName to keep data isolated.
 * Size guard: rejects writes if stored data exceeds MAX_STORE_BYTES.
 */
class EncryptedModuleStore(
    context: Context,
    prefsName: String,
    private val encryption: EncryptionPort,
    private val maxStoreBytes: Int = DEFAULT_MAX_STORE_BYTES
) {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun put(key: String, plaintext: ByteArray) {
        val encrypted = encryption.encrypt(plaintext)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)

        val currentSize = estimateCurrentSizeBytes()
        val newEntrySize = encoded.length
        if (currentSize + newEntrySize > maxStoreBytes) {
            throw IllegalStateException(
                "EncryptedModuleStore size limit exceeded for key=$key " +
                "(current=${currentSize}B, adding=${newEntrySize}B, max=${maxStoreBytes}B)"
            )
        }

        val ok = prefs.edit().putString(key, encoded).commit()
        if (!ok) throw IllegalStateException("EncryptedModuleStore put failed for key=$key")
    }

    fun get(key: String): ByteArray? {
        val encoded = prefs.getString(key, null) ?: return null
        return try {
            val encrypted = Base64.decode(encoded, Base64.NO_WRAP)
            encryption.decrypt(encrypted)
        } catch (_: Throwable) {
            null
        }
    }

    fun remove(key: String) {
        val ok = prefs.edit().remove(key).commit()
        if (!ok) throw IllegalStateException("EncryptedModuleStore remove failed for key=$key")
    }

    fun clearAll() {
        val ok = prefs.edit().clear().commit()
        if (!ok) throw IllegalStateException("EncryptedModuleStore clearAll failed")
    }

    fun containsKey(key: String): Boolean = prefs.contains(key)

    fun estimateCurrentSizeBytes(): Int {
        return prefs.all.values
            .filterIsInstance<String>()
            .sumOf { it.length }
    }

    companion object {
        const val DEFAULT_MAX_STORE_BYTES = 512 * 1024 // 512 KB per module store
    }
}
