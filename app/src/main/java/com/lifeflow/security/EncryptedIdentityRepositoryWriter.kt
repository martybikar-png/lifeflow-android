package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import java.util.UUID

/**
 * Consistency writer for encrypted identity blobs.
 *
 * Ordering is intentional:
 * 1) compute next version without mutating vault
 * 2) encrypt for that next version
 * 3) write blob
 * 4) commit vault version
 * 5) if vault commit fails, restore previous blob best-effort and fail closed
 */
internal fun encryptedIdentityWriteVersionedBlob(
    id: UUID,
    plain: ByteArray,
    operation: String,
    blobStore: EncryptedIdentityBlobStore,
    encryptionService: EncryptionService,
    vault: AndroidDataSovereigntyVault
) {
    val previousVersion = vault.getIdentityVersion(id)
    val nextVersion = previousVersion + 1L
    require(nextVersion > 0L) { "Version overflow for id=$id" }

    val previousStored = blobStore.get(id)

    val stored = try {
        encryptedIdentityEncryptVersionedBlob(
            id = id,
            version = nextVersion,
            plain = plain,
            encryptionService = encryptionService
        )
    } catch (t: Throwable) {
        throw SecurityException(
            "EncryptedIdentityRepository: $operation encrypt failed for id=$id",
            t
        )
    }

    try {
        blobStore.put(id, stored)

        val committedVersion = vault.nextIdentityVersion(id)
        check(committedVersion == nextVersion) {
            "Vault version mismatch for id=$id: expected=$nextVersion actual=$committedVersion"
        }
    } catch (t: Throwable) {
        encryptedIdentityRestorePreviousBlob(
            id = id,
            previousStored = previousStored,
            original = t,
            blobStore = blobStore
        )
        throw SecurityException(
            "EncryptedIdentityRepository: $operation consistency failure for id=$id",
            t
        )
    }
}

private fun encryptedIdentityRestorePreviousBlob(
    id: UUID,
    previousStored: ByteArray?,
    original: Throwable,
    blobStore: EncryptedIdentityBlobStore
) {
    runCatching {
        if (previousStored == null) {
            blobStore.delete(id)
        } else {
            blobStore.put(id, previousStored)
        }
    }.onFailure { rollbackFailure ->
        original.addSuppressed(rollbackFailure)
    }
}
