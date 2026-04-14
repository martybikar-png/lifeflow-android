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

        validateIv(iv)
        return wrapVersionedPayload(iv, cipherText)
    }

    fun decrypt(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        return decryptVersionedFormat(encryptedData, aad)
    }

    fun decryptVersionedFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        val payload = parseVersionedPayload(encryptedData)
        val cipher = createDecryptCipher(payload.iv)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    fun decryptLegacyFormat(encryptedData: ByteArray, aad: ByteArray? = null): ByteArray {
        val payload = parseLegacyPayload(encryptedData)
        val cipher = createDecryptCipher(payload.iv)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    /**
     * Prepares ENCRYPT_MODE cipher for future CryptoObject flow.
     */
    fun createEncryptCipher(): Cipher {
        val secretKey: SecretKey = keyManager.getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    /**
     * Prepares DECRYPT_MODE cipher from versioned payload for future CryptoObject flow.
     */
    fun createDecryptCipherVersionedFormat(encryptedData: ByteArray): Cipher {
        val payload = parseVersionedPayload(encryptedData)
        return createDecryptCipher(payload.iv)
    }

    /**
     * Prepares DECRYPT_MODE cipher from legacy payload for future CryptoObject flow.
     */
    fun createDecryptCipherLegacyFormat(encryptedData: ByteArray): Cipher {
        val payload = parseLegacyPayload(encryptedData)
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
        val payload = parseVersionedPayload(encryptedData)
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
        val payload = parseLegacyPayload(encryptedData)
        return decryptCipherPayload(cipher, payload.cipherText, aad)
    }

    private fun createDecryptCipher(iv: ByteArray): Cipher {
        validateIv(iv)

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

    private fun parseVersionedPayload(encryptedData: ByteArray): EncryptedPayload {
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

        validateIv(iv)
        return EncryptedPayload(iv = iv, cipherText = cipherText)
    }

    private fun parseLegacyPayload(encryptedData: ByteArray): EncryptedPayload {
        require(encryptedData.size > LEGACY_IV_LENGTH) { "Invalid legacy encrypted data" }

        val iv = encryptedData.copyOfRange(0, LEGACY_IV_LENGTH)
        val cipherText = encryptedData.copyOfRange(LEGACY_IV_LENGTH, encryptedData.size)

        validateIv(iv)
        return EncryptedPayload(iv = iv, cipherText = cipherText)
    }

    private fun wrapVersionedPayload(
        iv: ByteArray,
        cipherText: ByteArray
    ): ByteArray {
        return ByteBuffer
            .allocate(IV_LEN_PREFIX_BYTES + iv.size + cipherText.size)
            .put(iv.size.toByte())
            .put(iv)
            .put(cipherText)
            .array()
    }

    private fun validateIv(iv: ByteArray) {
        require(iv.size in MIN_IV_LEN..MAX_IV_LEN) {
            "Invalid IV length: ${iv.size}"
        }
    }

    private data class EncryptedPayload(
        val iv: ByteArray,
        val cipherText: ByteArray
    )
}
