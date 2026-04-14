package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetVaultUseCase(
    private val blobStore: EncryptedIdentityBlobStore,
    private val vault: AndroidDataSovereigntyVault
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        requireVerifiedTrustForVaultReset()
        SecurityAccessSession.requireAuthorized(
            reason = "Vault reset requires active auth session"
        )
        SecurityVaultResetAuthorization.consumeFreshAuthorization(
            reason = "Vault reset requires fresh elevated authentication"
        )

        SecurityAccessSession.clear()

        vault.resetVault()
        blobStore.clearAll()
        vault.ensureInitialized()

        SecurityRuleEngine.recoverAfterVaultReset(
            reason = "Vault reset"
        )
    }

    private fun requireVerifiedTrustForVaultReset() {
        if (SecurityRuleEngine.getTrustState() != TrustState.VERIFIED) {
            throw SecurityException(
                "Vault reset denied: trust state must be VERIFIED."
            )
        }
    }
}
