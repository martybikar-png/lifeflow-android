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

        // Legacy format assumed 12-byte IV
        private const val LEGACY_IV_LENGTH = 12

        // New format: [1 byte ivLen] + iv + ciphertext
        private const val IV_LEN_PREFIX_BYTES = 1
        private const val MIN_IV_LEN = 12
        private const val MAX_IV_LEN = 16
    }

    /**
     * Encrypts plaintext using AES-256-GCM (Android Keystore).
     * Returns (V1 format): [1 byte ivLen] + IV + Ciphertext
     */
    fun encrypt(plainText: ByteArray, aad: ByteArray? = null): ByteArray {
        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Let provider generate a randomized IV (required by Keystore policy)
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
     * Supports:
     * - New format: [1 byte ivLen] + IV + Ciphertext
     * - Legacy format: IV(12 bytes) + Ciphertext
     */
    fun decrypt(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        require(encryptedData.isNotEmpty()) { "Invalid encrypted data" }

        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val first = encryptedData[0].toInt() and 0xFF
        val useNewFormat = first in MIN_IV_LEN..MAX_IV_LEN && encryptedData.size > 1 + first

        val iv: ByteArray
        val cipherText: ByteArray

        if (useNewFormat) {
            iv = encryptedData.copyOfRange(1, 1 + first)
            cipherText = encryptedData.copyOfRange(1 + first, encryptedData.size)
        } else {
            require(encryptedData.size > LEGACY_IV_LENGTH) { "Invalid legacy encrypted data" }
            iv = encryptedData.copyOfRange(0, LEGACY_IV_LENGTH)
            cipherText = encryptedData.copyOfRange(LEGACY_IV_LENGTH, encryptedData.size)
        }

        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        return cipher.doFinal(cipherText)
    }
}