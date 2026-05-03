package com.lifeflow.security

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionService(
    private val keyManager: KeyManager
) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    fun encrypt(plainText: ByteArray, aad: ByteArray? = null): ByteArray {
        val cipher = createEncryptCipher()
        return encryptWithCipher(cipher, plainText, aad)
    }

    /**
     * Encrypts using a caller-provided cipher.
     * This is the foundation for future CryptoObject / auth-per-use flow.
     */
    fun encryptWithCipher(
        cipher: Cipher,
        plainText: ByteArray,
        aad: ByteArray? = null
    ): ByteArray {
        if (aad != null) {
            cipher.updateAAD(aad)
        }

        val cipherText = cipher.doFinal(plainText)
        val iv = cipher.iv ?: throw IllegalStateException("Cipher IV missing after init(ENCRYPT_MODE)")

        validateEncryptionPayloadIv(iv)
        return wrapEncryptionVersionedPayload(iv, cipherText)
    }

    fun decrypt(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        return decryptVersionedFormat(encryptedData, aad)
    }

    fun decryptVersionedFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        val payload = parseEncryptionVersionedPayload(encryptedData)
        val cipher = createDecryptCipher(payload.iv)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    fun decryptLegacyFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        val payload = parseEncryptionLegacyPayload(encryptedData)
        val cipher = createDecryptCipher(payload.iv)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    /**
     * Prepares ENCRYPT_MODE cipher for future CryptoObject flow.
     */
    fun createEncryptCipher(): Cipher {
        keyManager.requireOperationalKeyPosture()

        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    /**
     * Prepares DECRYPT_MODE cipher from versioned payload for future CryptoObject flow.
     */
    fun createDecryptCipherVersionedFormat(encryptedData: ByteArray): Cipher {
        val payload = parseEncryptionVersionedPayload(encryptedData)
        return createDecryptCipher(payload.iv)
    }

    /**
     * Prepares DECRYPT_MODE cipher from legacy payload for future CryptoObject flow.
     */
    fun createDecryptCipherLegacyFormat(encryptedData: ByteArray): Cipher {
        val payload = parseEncryptionLegacyPayload(encryptedData)
        return createDecryptCipher(payload.iv)
    }

    /**
     * Decrypts versioned payload using a caller-provided cipher.
     * Intended for future BiometricPrompt.CryptoObject success path.
     */
    fun decryptWithCipherVersionedFormat(
        cipher: Cipher,
        encryptedData: ByteArray,
        aad: ByteArray? = null
    ): ByteArray {
        val payload = parseEncryptionVersionedPayload(encryptedData)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    /**
     * Decrypts legacy payload using a caller-provided cipher.
     * Intended for future BiometricPrompt.CryptoObject success path.
     */
    fun decryptWithCipherLegacyFormat(
        cipher: Cipher,
        encryptedData: ByteArray,
        aad: ByteArray? = null
    ): ByteArray {
        val payload = parseEncryptionLegacyPayload(encryptedData)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    private fun createDecryptCipher(iv: ByteArray): Cipher {
        validateEncryptionPayloadIv(iv)
        keyManager.requireOperationalKeyPosture()

        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher
    }

    private fun decryptCipherPayload(
        cipher: Cipher,
        cipherText: ByteArray,
        aad: ByteArray?
    ): ByteArray {
        require(cipherText.isNotEmpty()) {
            "Missing ciphertext payload"
        }

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        return cipher.doFinal(cipherText)
    }
}
