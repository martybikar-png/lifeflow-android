package com.lifeflow.security

import com.lifeflow.domain.model.LifeFlowIdentity
import java.nio.charset.StandardCharsets
import java.util.UUID

private const val MARKER_VERSIONED: Int = 0xA1
private const val MARKER_LEGACY: Int = 0xA2

private val SCHEME_VERSIONED: Byte = MARKER_VERSIONED.toByte()
private val SCHEME_LEGACY: Byte = MARKER_LEGACY.toByte()

/**
 * Strict decryption:
 * - Reads explicit scheme marker if present
 * - NO downgrade fallback
 * - If blob is versioned, requires version > 0
 * - If blob is legacy (or unmarked legacy), uses legacy AAD
 * - Read paths stay read-only; no migration side effects here
 */
internal fun encryptedIdentityDecryptStrict(
    id: UUID,
    version: Long,
    stored: ByteArray,
    encryptionService: EncryptionService
): ByteArray {
    val (scheme, cipher) = encryptedIdentityUnwrapSchemeMarker(stored)

    return when (scheme) {
        SCHEME_VERSIONED -> {
            require(version > 0L) { "Missing vault version for versioned blob id=$id" }
            encryptionService.decryptVersionedFormat(
                cipher,
                encryptedIdentityAadVersioned(id, version)
            )
        }

        SCHEME_LEGACY -> {
            encryptionService.decryptLegacyFormat(cipher, encryptedIdentityAadLegacy(id))
        }

        else -> throw SecurityException("Unknown blob scheme marker for id=$id")
    }
}

internal fun encryptedIdentityEncryptVersionedBlob(
    id: UUID,
    version: Long,
    plain: ByteArray,
    encryptionService: EncryptionService
): ByteArray {
    require(version > 0L) { "Missing vault version for versioned blob id=$id" }

    val cipher = encryptionService.encrypt(
        plain,
        encryptedIdentityAadVersioned(id, version)
    )
    return encryptedIdentityWrapWithSchemeMarker(SCHEME_VERSIONED, cipher)
}

internal fun encryptedIdentitySerialize(identity: LifeFlowIdentity): ByteArray {
    val serialized = buildString {
        append(identity.id.toString())
        append("|")
        append(identity.createdAtEpochMillis)
        append("|")
        append(identity.isActive)
    }
    return serialized.toByteArray(StandardCharsets.UTF_8)
}

internal fun encryptedIdentityDeserialize(bytes: ByteArray): LifeFlowIdentity {
    val serialized = String(bytes, StandardCharsets.UTF_8)
    val parts = serialized.split("|")
    require(parts.size == 3) { "Invalid identity payload" }

    val id = UUID.fromString(parts[0])
    val createdAt = parts[1].toLong()
    val isActive = parts[2].toBooleanStrictOrNull()
        ?: throw IllegalArgumentException("Invalid identity active flag")

    return LifeFlowIdentity(
        id = id,
        createdAtEpochMillis = createdAt,
        isActive = isActive
    )
}

internal fun encryptedIdentityAadLegacyForTests(id: UUID): ByteArray {
    return encryptedIdentityAadLegacy(id)
}

internal fun encryptedIdentityAadVersionedForTests(
    id: UUID,
    version: Long
): ByteArray {
    return encryptedIdentityAadVersioned(id, version)
}

internal fun encryptedIdentityWrapWithSchemeMarkerForTests(
    marker: Byte,
    cipher: ByteArray
): ByteArray {
    return encryptedIdentityWrapWithSchemeMarker(marker, cipher)
}

internal fun encryptedIdentityUnwrapSchemeMarkerForTests(
    stored: ByteArray
): Pair<Byte, ByteArray> {
    return encryptedIdentityUnwrapSchemeMarker(stored)
}

private fun encryptedIdentityAadLegacy(id: UUID): ByteArray {
    return id.toString().toByteArray(StandardCharsets.UTF_8)
}

private fun encryptedIdentityAadVersioned(
    id: UUID,
    version: Long
): ByteArray {
    return "${id}|v$version".toByteArray(StandardCharsets.UTF_8)
}

private fun encryptedIdentityWrapWithSchemeMarker(
    marker: Byte,
    cipher: ByteArray
): ByteArray {
    val output = ByteArray(1 + cipher.size)
    output[0] = marker
    System.arraycopy(cipher, 0, output, 1, cipher.size)
    return output
}

private fun encryptedIdentityUnwrapSchemeMarker(
    stored: ByteArray
): Pair<Byte, ByteArray> {
    if (stored.isEmpty()) {
        throw IllegalArgumentException("Empty stored blob")
    }

    val first = stored[0]
    return when (first) {
        SCHEME_VERSIONED, SCHEME_LEGACY -> {
            require(stored.size > 1) { "Missing cipher payload for marked blob" }
            val cipher = stored.copyOfRange(1, stored.size)
            first to cipher
        }

        else -> {
            SCHEME_LEGACY to stored
        }
    }
}