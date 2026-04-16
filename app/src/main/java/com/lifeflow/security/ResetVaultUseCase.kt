package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetVaultUseCase(
    private val blobStore: EncryptedIdentityBlobStore,
    private val vault: AndroidDataSovereigntyVault
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        requireVaultResetAccess()
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

    private fun requireVaultResetAccess() {
        val sessionAuthorized = SecurityAccessSession.isAuthorized()

        val decision = SecurityRuntimeAccessPolicy.decideAuthorization(
            request = SecurityRuntimeAuthorizationRequest(
                isReadOperation = false,
                requiresStrictAuth = true,
                trustedBaseOnly = false,
                hasRecentAuthentication = sessionAuthorized
            )
        )

        when (decision.outcome) {
            SecurityRuntimeAuthorizationOutcome.ALLOWED -> return

            SecurityRuntimeAuthorizationOutcome.LOCKED -> {
                throw SecurityException(
                    when (decision.code) {
                        "COMPROMISED" ->
                            "Vault reset denied: security is compromised."

                        "RECOVERY_REQUIRED" ->
                            "Vault reset denied: recovery is required before vault reset can proceed."

                        else ->
                            "Vault reset denied: protected runtime is locked."
                    }
                )
            }

            SecurityRuntimeAuthorizationOutcome.DENIED,
            SecurityRuntimeAuthorizationOutcome.REQUIRES_ELEVATION -> {
                throw SecurityException(
                    when (decision.code) {
                        "RECENT_AUTH_REQUIRED",
                        "AUTH_CONTEXT_INVALID" ->
                            "Vault reset denied: active auth session is required."

                        "STRONGER_AUTH_REQUIRED",
                        "TRUST_NOT_SUFFICIENT",
                        "TRUSTED_BASE_ONLY_REQUIRED",
                        "EMERGENCY_APPROVAL_REQUIRED" ->
                            "Vault reset denied: trust state must be VERIFIED."

                        else ->
                            "Vault reset denied: current security posture does not allow vault reset."
                    }
                )
            }

            SecurityRuntimeAuthorizationOutcome.REQUIRES_EMERGENCY_RESOLUTION -> {
                throw SecurityException(
                    "Vault reset denied: current security posture does not allow vault reset."
                )
            }
        }
    }
}
