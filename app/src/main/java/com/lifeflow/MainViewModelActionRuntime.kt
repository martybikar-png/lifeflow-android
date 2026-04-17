package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator

internal class MainViewModelActionRuntime(
    private val orchestrator: LifeFlowOrchestrator,
    private val wellbeingDelegate: MainViewModelWellbeingDelegate,
    private val authDelegate: MainViewModelAuthDelegate,
    private val performVaultReset: suspend () -> Unit,
    private val updateLastAction: (String) -> Unit,
    private val isFreeTier: () -> Boolean,
    private val isAuthenticatedUi: () -> Boolean,
    private val activateFreeTierUi: () -> Unit,
    private val markAuthenticated: () -> Unit
) {
    suspend fun triggerRuntimeRefresh(lastActionMessage: String) {
        updateLastAction(lastActionMessage)

        if (isFreeTier()) {
            wellbeingDelegate.refreshPublicHealthStateOnly()
            return
        }

        refreshProtectedSnapshot(identityInitialized = isAuthenticatedUi())
    }

    suspend fun refreshProtectedSnapshot(identityInitialized: Boolean) {
        runCatching {
            wellbeingDelegate.refreshWellbeingSnapshotSafe(
                identityInitialized = identityInitialized
            )
        }.onFailure {
            wellbeingDelegate.handleUnexpectedProtectedRefreshFailure()
        }
    }

    suspend fun runAuthenticationBootstrap() {
        if (isFreeTier()) {
            authDelegate.clearSessionExpiryNotification()
            activateFreeTierUi()
            return
        }

        if (!authDelegate.ensureRuntimeEntryAllowed()) return
        authDelegate.clearSessionExpiryNotification()

        when (val boot = orchestrator.bootstrapIdentityIfNeeded()) {
            is ActionResult.Success -> {
                authDelegate.completeAuthenticationBootstrapSuccess(
                    markAuthenticated = markAuthenticated
                )
            }

            is ActionResult.Locked -> {
                authDelegate.completeAuthenticationBootstrapLocked(
                    message = mainViewModelLockedReasonToUserMessage(boot.reason)
                )
            }

            is ActionResult.Error -> {
                authDelegate.completeAuthenticationBootstrapError(boot.message)
            }
        }
    }

    suspend fun runVaultReset() {
        authDelegate.completeVaultReset(
            result = runCatching { performVaultReset() }
        )
    }
}
