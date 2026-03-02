package com.lifeflow.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionService(
    private val keyManager: KeyManager
) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12
    }

    private val secureRandom = SecureRandom()

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns: IV + Ciphertext (combined)
     */
    fun encrypt(plainText: ByteArray, aad: ByteArray? = null): ByteArray {
        val secretKey: SecretKey = keyManager.getKey()

        val cipher = Cipher.getInstance(TRANSFORMATION)

        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        val cipherText = cipher.doFinal(plainText)

        return ByteBuffer
            .allocate(iv.size + cipherText.size)
            .put(iv)
            .put(cipherText)
            .array()
    }

    /**
     * Decrypts data produced by encrypt().
     * Expects: IV + Ciphertext (combined)
     */
    fun decrypt(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        require(encryptedData.size > IV_LENGTH) {
            "Invalid encrypted data"
        }

        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val iv = encryptedData.copyOfRange(0, IV_LENGTH)
        val cipherText = encryptedData.copyOfRange(IV_LENGTH, encryptedData.size)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        return cipher.doFinal(cipherText)
    }
}