package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetVaultUseCase(
    private val blobStore: EncryptedIdentityBlobStore,
    private val vault: AndroidDataSovereigntyVault
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        requireVaultResetPosture()
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

    private fun requireVaultResetPosture() {
        when (SecurityRuleEngine.getTrustState()) {
            TrustState.VERIFIED,
            TrustState.DEGRADED,
            TrustState.COMPROMISED -> return

            TrustState.EMERGENCY_LIMITED -> {
                throw SecurityException(
                    "Vault reset denied: emergency limited mode must be cleared before vault reset."
                )
            }
        }
    }
}
