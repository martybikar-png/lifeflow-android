package com.lifeflow.data.store

import android.content.Context
import android.util.Base64
import com.lifeflow.domain.core.EncryptionPort

/**
 * EncryptedModuleStore — generic encrypted key-value store for module data.
 *
 * Uses EncryptionPort (AES-256-GCM via domain boundary) for all writes.
 * Fail-closed on decrypt errors — returns null.
 * Fail-closed on write errors — throws.
 *
 * Each module uses its own prefsName to keep data isolated.
 */
class EncryptedModuleStore(
    context: Context,
    prefsName: String,
    private val encryption: EncryptionPort
) {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun put(key: String, plaintext: ByteArray) {
        val encrypted = encryption.encrypt(plaintext)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)
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
}
