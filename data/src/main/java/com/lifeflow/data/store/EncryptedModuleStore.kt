package com.lifeflow.data.store

import android.content.Context
import android.content.SharedPreferences
import com.lifeflow.domain.core.EncryptionPort
import java.util.Base64

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
class EncryptedModuleStore internal constructor(
    private val prefs: SharedPreferences,
    private val encryption: EncryptionPort,
    private val maxStoreBytes: Int = DEFAULT_MAX_STORE_BYTES
) {
    constructor(
        context: Context,
        prefsName: String,
        encryption: EncryptionPort,
        maxStoreBytes: Int = DEFAULT_MAX_STORE_BYTES
    ) : this(
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE),
        encryption = encryption,
        maxStoreBytes = maxStoreBytes
    )

    fun put(key: String, plaintext: ByteArray) {
        val encrypted = encryption.encrypt(plaintext)
        val encoded = Base64.getEncoder().encodeToString(encrypted)
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
            val encrypted = Base64.getDecoder().decode(encoded)
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
        const val DEFAULT_MAX_STORE_BYTES = 512 * 1024
    }
}
