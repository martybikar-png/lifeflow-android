package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.security.DomainOperation
import java.util.UUID
import kotlinx.coroutines.CancellationException

internal fun encryptedIdentityReadById(
    id: UUID,
    blobStore: EncryptedIdentityBlobStore,
    encryptionService: EncryptionService,
    vault: AndroidDataSovereigntyVault
): LifeFlowIdentity? {
    SecurityRuleEngine.requireAllowed(
        operation = DomainOperation.READ_IDENTITY_BY_ID,
        reason = "getById(id) requires active auth session"
    )

    val stored = blobStore.get(id) ?: return null
    val version = vault.getIdentityVersion(id)

    try {
        val plain = encryptedIdentityDecryptStrict(
            id = id,
            version = version,
            stored = stored,
            encryptionService = encryptionService
        )
        val identity = encryptedIdentityDeserialize(plain)
        require(identity.id == id) { "Identity id mismatch for requested id=$id" }
        return identity
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        SecurityKeystoreFailureHandler.throwForFailure(
            operation = DomainOperation.READ_IDENTITY_BY_ID,
            failureReason = "decrypt/deserialize failed for id=$id",
            genericMessage = "EncryptedIdentityRepository: getById() integrity failure",
            throwable = exception
        )
    }
}

internal fun encryptedIdentityReadActiveIdentity(
    blobStore: EncryptedIdentityBlobStore,
    encryptionService: EncryptionService,
    vault: AndroidDataSovereigntyVault
): LifeFlowIdentity? {
    SecurityRuleEngine.requireAllowed(
        operation = DomainOperation.READ_ACTIVE_IDENTITY,
        reason = "getActiveIdentity() requires active auth session"
    )

    try {
        for ((id, stored) in blobStore.entries()) {
            val version = vault.getIdentityVersion(id)
            val plain = encryptedIdentityDecryptStrict(
                id = id,
                version = version,
                stored = stored,
                encryptionService = encryptionService
            )
            val identity = encryptedIdentityDeserialize(plain)

            if (identity.id != id) {
                throw SecurityException("Identity id mismatch during active scan for id=$id")
            }

            if (identity.isActive) return identity
        }
        return null
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        SecurityKeystoreFailureHandler.throwForFailure(
            operation = DomainOperation.READ_ACTIVE_IDENTITY,
            failureReason = "decrypt/deserialize failed during scan",
            genericMessage = "EncryptedIdentityRepository: getActiveIdentity() integrity failure",
            throwable = exception
        )
    }
}
