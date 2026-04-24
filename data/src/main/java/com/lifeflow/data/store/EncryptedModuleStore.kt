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
 * Binding model:
 * - New writes are stored as LFMS + scheme + ciphertext
 * - Ciphertext is encrypted with AAD(storeName + key + bindingId)
 * - Legacy entries without LFMS header are read once without AAD and then
 *   rewritten into the bound format on successful read
 *
 * Each module uses its own prefsName to keep data isolated.
 * Size guard: rejects writes if stored data exceeds MAX_STORE_BYTES.
 */
class EncryptedModuleStore internal constructor(
    private val prefs: SharedPreferences,
    private val storeName: String,
    private val encryption: EncryptionPort,
    private val maxStoreBytes: Int = DEFAULT_MAX_STORE_BYTES,
    private val deviceBindingContextProvider: (() -> String)? = null
) {
    init {
        require(storeName.isNotBlank()) {
            "EncryptedModuleStore storeName must not be blank."
        }
    }

    constructor(
        context: Context,
        prefsName: String,
        encryption: EncryptionPort,
        maxStoreBytes: Int = DEFAULT_MAX_STORE_BYTES,
        deviceBindingContextProvider: (() -> String)? = null
    ) : this(
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE),
        storeName = prefsName,
        encryption = encryption,
        maxStoreBytes = maxStoreBytes,
        deviceBindingContextProvider = deviceBindingContextProvider
    )

    fun put(key: String, plaintext: ByteArray) {
        val encrypted = encryption.encrypt(
            plaintext = plaintext,
            aad = aadFor(key)
        )
        val wrapped = wrapBoundPayload(encrypted)
        val encoded = Base64.getEncoder().encodeToString(wrapped)

        val currentSize = estimateCurrentSizeBytes()
        val existingEntrySize = prefs.getString(key, null)?.length ?: 0
        val projectedSize = currentSize - existingEntrySize + encoded.length

        if (projectedSize > maxStoreBytes) {
            throw IllegalStateException(
                "EncryptedModuleStore size limit exceeded for key=$key " +
                    "(current=${currentSize}B, replacing=${existingEntrySize}B, new=${encoded.length}B, max=${maxStoreBytes}B)"
            )
        }

        val ok = prefs.edit().putString(key, encoded).commit()
        if (!ok) {
            throw IllegalStateException("EncryptedModuleStore put failed for key=$key")
        }
    }

    fun get(key: String): ByteArray? {
        val encoded = prefs.getString(key, null) ?: return null

        return try {
            val stored = Base64.getDecoder().decode(encoded)

            when (val payload = parseStoredPayload(stored)) {
                is StoredPayload.BoundV1 -> encryption.decrypt(
                    ciphertext = payload.ciphertext,
                    aad = aadFor(key)
                )

                is StoredPayload.LegacyRaw -> {
                    val plaintext = encryption.decrypt(payload.ciphertext)
                    migrateLegacyEntry(
                        key = key,
                        plaintext = plaintext
                    )
                    plaintext
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun remove(key: String) {
        val ok = prefs.edit().remove(key).commit()
        if (!ok) {
            throw IllegalStateException("EncryptedModuleStore remove failed for key=$key")
        }
    }

    fun clearAll() {
        val ok = prefs.edit().clear().commit()
        if (!ok) {
            throw IllegalStateException("EncryptedModuleStore clearAll failed")
        }
    }

    fun containsKey(key: String): Boolean =
        prefs.contains(key)

    fun estimateCurrentSizeBytes(): Int {
        return prefs.all.values
            .filterIsInstance<String>()
            .sumOf { it.length }
    }

    private fun aadFor(key: String): ByteArray {
        require(key.isNotBlank()) {
            "EncryptedModuleStore key must not be blank."
        }

        val bindingSegment = resolveBindingSegment()

        return buildString {
            append(MODULE_STORE_AAD_CONTEXT)
            append('|')
            append(storeName)
            append('|')
            append(key)
            append('|')
            append(bindingSegment)
        }.toByteArray(Charsets.UTF_8)
    }

    private fun resolveBindingSegment(): String {
        val provider = deviceBindingContextProvider ?: return UNBOUND_BINDING_SEGMENT
        val bindingId = provider.invoke().trim()

        require(bindingId.isNotBlank()) {
            "Device binding context is blank for store=$storeName"
        }

        return bindingId
    }

    private fun migrateLegacyEntry(
        key: String,
        plaintext: ByteArray
    ) {
        runCatching {
            put(
                key = key,
                plaintext = plaintext
            )
        }
    }

    private fun wrapBoundPayload(
        ciphertext: ByteArray
    ): ByteArray {
        val output = ByteArray(STORE_HEADER.size + 1 + ciphertext.size)
        STORE_HEADER.copyInto(output, destinationOffset = 0)
        output[STORE_HEADER.size] = SCHEME_BOUND_V1
        ciphertext.copyInto(output, destinationOffset = STORE_HEADER.size + 1)
        return output
    }

    private fun parseStoredPayload(
        stored: ByteArray
    ): StoredPayload {
        if (hasStoreHeader(stored)) {
            val scheme = stored[STORE_HEADER.size]
            val ciphertextStart = STORE_HEADER.size + 1

            require(stored.size > ciphertextStart) {
                "EncryptedModuleStore payload is missing ciphertext."
            }

            val ciphertext = stored.copyOfRange(ciphertextStart, stored.size)

            return when (scheme) {
                SCHEME_BOUND_V1 -> StoredPayload.BoundV1(ciphertext)
                else -> throw IllegalStateException(
                    "Unsupported EncryptedModuleStore payload scheme for store=$storeName"
                )
            }
        }

        return StoredPayload.LegacyRaw(stored)
    }

    private fun hasStoreHeader(
        stored: ByteArray
    ): Boolean {
        if (stored.size <= STORE_HEADER.size) {
            return false
        }

        return STORE_HEADER.indices.all { index ->
            stored[index] == STORE_HEADER[index]
        }
    }

    private sealed interface StoredPayload {
        data class BoundV1(
            val ciphertext: ByteArray
        ) : StoredPayload

        data class LegacyRaw(
            val ciphertext: ByteArray
        ) : StoredPayload
    }

    companion object {
        const val DEFAULT_MAX_STORE_BYTES = 512 * 1024

        private val STORE_HEADER = byteArrayOf(
            0x4C,
            0x46,
            0x4D,
            0x53
        ) // LFMS

        private const val SCHEME_BOUND_V1: Byte = 0x01
        private const val MODULE_STORE_AAD_CONTEXT = "lifeflow-module-store-v1"
        private const val UNBOUND_BINDING_SEGMENT = "unbound"
    }
}
