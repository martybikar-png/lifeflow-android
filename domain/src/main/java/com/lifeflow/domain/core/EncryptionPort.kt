package com.lifeflow.domain.core

/**
 * EncryptionPort — domain boundary for encryption operations.
 *
 * Allows data layer to encrypt/decrypt without depending on
 * the security implementation directly.
 */
interface EncryptionPort {
    fun encrypt(plaintext: ByteArray): ByteArray =
        encrypt(
            plaintext = plaintext,
            aad = null
        )

    fun decrypt(ciphertext: ByteArray): ByteArray =
        decrypt(
            ciphertext = ciphertext,
            aad = null
        )

    fun encrypt(
        plaintext: ByteArray,
        aad: ByteArray?
    ): ByteArray

    fun decrypt(
        ciphertext: ByteArray,
        aad: ByteArray?
    ): ByteArray
}
