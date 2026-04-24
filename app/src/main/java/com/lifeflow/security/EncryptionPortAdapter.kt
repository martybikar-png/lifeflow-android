package com.lifeflow.security

import com.lifeflow.domain.core.EncryptionPort

/**
 * EncryptionPortAdapter — bridges EncryptionService to EncryptionPort.
 * Allows data layer to use encryption without direct security dependency.
 */
class EncryptionPortAdapter(
    private val encryptionService: EncryptionService
) : EncryptionPort {

    override fun encrypt(plaintext: ByteArray): ByteArray =
        encryptionService.encrypt(plaintext)

    override fun decrypt(ciphertext: ByteArray): ByteArray =
        encryptionService.decrypt(ciphertext)

    override fun encrypt(
        plaintext: ByteArray,
        aad: ByteArray?
    ): ByteArray = encryptionService.encrypt(
        plainText = plaintext,
        aad = aad
    )

    override fun decrypt(
        ciphertext: ByteArray,
        aad: ByteArray?
    ): ByteArray = encryptionService.decrypt(
        encryptedData = ciphertext,
        aad = aad
    )
}
