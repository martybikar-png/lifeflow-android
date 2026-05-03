package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator

internal suspend fun runMainViewModelAuthenticationBootstrap(
    orchestrator: LifeFlowOrchestrator,
    refreshTierAndBoundaryState: () -> Unit,
    isFreeTier: () -> Boolean,
    clearSessionExpiryNotification: () -> Unit,
    activateFreeTierUi: () -> Unit,
    ensureRuntimeEntryAllowed: () -> Boolean,
    markAuthenticated: () -> Unit,
    onLocked: (String) -> Unit,
    onError: (String) -> Unit,
    updateLastAction: (String) -> Unit
) {
    refreshTierAndBoundaryState()

    if (isFreeTier()) {
        clearSessionExpiryNotification()
        activateFreeTierUi()
        return
    }

    if (!ensureRuntimeEntryAllowed()) return
    clearSessionExpiryNotification()

    when (val boot = orchestrator.bootstrapIdentityIfNeeded()) {
        is ActionResult.Success -> {
            refreshTierAndBoundaryState()
            markAuthenticated()
            updateLastAction("Protected dashboard unlocked.")
        }

        is ActionResult.Locked -> {
            onLocked(mainViewModelLockedReasonToUserMessage(boot.reason))
        }

        is ActionResult.Error -> {
            onError(boot.message)
        }
    }
}
