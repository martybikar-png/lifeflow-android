package com.lifeflow.security

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionService(
    private val keyManager: KeyManager
) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128

        // Legacy format: IV(12 bytes) + ciphertext
        private const val LEGACY_IV_LENGTH = 12

        // Versioned format: [1 byte ivLen] + iv + ciphertext
        private const val IV_LEN_PREFIX_BYTES = 1
        private const val MIN_IV_LEN = 12
        private const val MAX_IV_LEN = 16
    }

    /**
     * Encrypts plaintext using AES-256-GCM (Android Keystore).
     * Returns versioned format:
     * [1 byte ivLen] + IV + ciphertext
     */
    fun encrypt(plainText: ByteArray, aad: ByteArray? = null): ByteArray {
        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Provider generates randomized IV (required by Keystore policy)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        val cipherText = cipher.doFinal(plainText)
        val iv = cipher.iv ?: throw IllegalStateException("Cipher IV missing after init(ENCRYPT_MODE)")

        require(iv.size in MIN_IV_LEN..MAX_IV_LEN) {
            "Unexpected IV length: ${iv.size}"
        }

        return ByteBuffer
            .allocate(IV_LEN_PREFIX_BYTES + iv.size + cipherText.size)
            .put(iv.size.toByte())
            .put(iv)
            .put(cipherText)
            .array()
    }

    /**
     * Decrypts data produced by encrypt().
     * This method is intentionally strict: versioned format only.
     */
    fun decrypt(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        return decryptVersionedFormat(encryptedData, aad)
    }

    /**
     * Deterministic decrypt for versioned format only:
     * [1 byte ivLen] + IV + ciphertext
     */
    fun decryptVersionedFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        require(encryptedData.size > IV_LEN_PREFIX_BYTES) { "Invalid versioned encrypted data" }

        val ivLen = encryptedData[0].toInt() and 0xFF
        require(ivLen in MIN_IV_LEN..MAX_IV_LEN) {
            "Invalid versioned IV length prefix: $ivLen"
        }
        require(encryptedData.size > IV_LEN_PREFIX_BYTES + ivLen) {
            "Invalid versioned encrypted data length"
        }

        val iv = encryptedData.copyOfRange(IV_LEN_PREFIX_BYTES, IV_LEN_PREFIX_BYTES + ivLen)
        val cipherText = encryptedData.copyOfRange(
            IV_LEN_PREFIX_BYTES + ivLen,
            encryptedData.size
        )

        return decryptWithIv(
            iv = iv,
            cipherText = cipherText,
            aad = aad
        )
    }

    /**
     * Deterministic decrypt for legacy format only:
     * IV(12 bytes) + ciphertext
     */
    fun decryptLegacyFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        require(encryptedData.size > LEGACY_IV_LENGTH) { "Invalid legacy encrypted data" }

        val iv = encryptedData.copyOfRange(0, LEGACY_IV_LENGTH)
        val cipherText = encryptedData.copyOfRange(LEGACY_IV_LENGTH, encryptedData.size)

        return decryptWithIv(
            iv = iv,
            cipherText = cipherText,
            aad = aad
        )
    }

    private fun decryptWithIv(
        iv: ByteArray,
        cipherText: ByteArray,
        aad: ByteArray?
    ): ByteArray {
        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        return cipher.doFinal(cipherText)
    }
}