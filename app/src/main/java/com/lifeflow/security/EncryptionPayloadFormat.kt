package com.lifeflow.security

import java.nio.ByteBuffer

internal data class EncryptionPayload(
    val iv: ByteArray,
    val cipherText: ByteArray
)

internal fun parseEncryptionVersionedPayload(
    encryptedData: ByteArray
): EncryptionPayload {
    require(encryptedData.size > IV_LEN_PREFIX_BYTES) {
        "Invalid versioned encrypted data"
    }

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

    validateEncryptionPayloadIv(iv)
    return EncryptionPayload(iv = iv, cipherText = cipherText)
}

internal fun parseEncryptionLegacyPayload(
    encryptedData: ByteArray
): EncryptionPayload {
    require(encryptedData.size > LEGACY_IV_LENGTH) {
        "Invalid legacy encrypted data"
    }

    val iv = encryptedData.copyOfRange(0, LEGACY_IV_LENGTH)
    val cipherText = encryptedData.copyOfRange(LEGACY_IV_LENGTH, encryptedData.size)

    validateEncryptionPayloadIv(iv)
    return EncryptionPayload(iv = iv, cipherText = cipherText)
}

internal fun wrapEncryptionVersionedPayload(
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

internal fun validateEncryptionPayloadIv(iv: ByteArray) {
    require(iv.size in MIN_IV_LEN..MAX_IV_LEN) {
        "Invalid IV length: ${iv.size}"
    }
}

// Legacy format: IV(12 bytes) + ciphertext
private const val LEGACY_IV_LENGTH = 12

// Versioned format: [1 byte ivLen] + iv + ciphertext
private const val IV_LEN_PREFIX_BYTES = 1
private const val MIN_IV_LEN = 12
private const val MAX_IV_LEN = 16
