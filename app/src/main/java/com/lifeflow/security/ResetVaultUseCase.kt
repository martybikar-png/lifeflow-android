package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetVaultUseCase(
    private val blobStore: EncryptedIdentityBlobStore,
    private val vault: AndroidDataSovereigntyVault
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        SecurityAccessSession.clear()

        vault.resetVault()
        blobStore.clearAll()
        vault.ensureInitialized()

        SecurityRuleEngine.recoverAfterVaultReset(
            reason = "Vault reset"
        )
    }
}