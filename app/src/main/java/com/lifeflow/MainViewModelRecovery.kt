package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.security.TrustState

internal fun wipeMainViewModelUiCachesFailClosed(
    wellbeingState: MainViewModelWellbeingState,
    updateLastAction: (String) -> Unit
) {
    applyMainViewModelWellbeingUiUpdate(
        update = mainViewModelFailClosedUiUpdate(),
        healthConnectStateState = wellbeingState.healthConnectState,
        requiredHealthPermissionsState = wellbeingState.requiredHealthPermissions,
        grantedHealthPermissionsState = wellbeingState.grantedHealthPermissions,
        healthPermissionsInitErrorState = wellbeingState.healthPermissionsInitError,
        digitalTwinStateState = wellbeingState.digitalTwinState,
        wellbeingAssessmentState = wellbeingState.wellbeingAssessment,
        updateLastAction = updateLastAction
    )
}

internal fun applyMainViewModelFailClosedState(
    clearSession: Boolean,
    clearSessionAction: () -> Unit,
    wipeUiCachesFailClosed: () -> Unit,
    setSessionExpiryNotified: (Boolean) -> Unit
) {
    if (clearSession) {
        clearSessionAction()
    }

    wipeUiCachesFailClosed()
    setSessionExpiryNotified(false)
}

internal fun failClosedMainViewModelWithError(
    message: String,
    clearSession: Boolean,
    applyFailClosedState: (Boolean) -> Unit,
    setUiError: (String) -> Unit,
    updateLastAction: (String) -> Unit
) {
    applyFailClosedState(clearSession)
    setUiError(message)
    updateLastAction("Fail-closed: $message")
}

internal fun ensureMainViewModelRuntimeEntryAllowed(
    securityEvaluation: MainViewModelSecurityEvaluation,
    updateLastAction: (String) -> Unit,
    failClosedAuthentication: (String, Boolean) -> Unit
): Boolean {
    val blockedMessage = securityEvaluation.runtimeEntryBlockMessage

    return if (blockedMessage == null) {
        updateLastAction("Authentication verified. Bootstrapping identity...")
        true
    } else {
        failClosedAuthentication(blockedMessage, true)
        false
    }
}

internal fun handleMainViewModelObservedTrustState(
    trustState: TrustState,
    isAuthenticatedUi: Boolean,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    val trustMessage = mainViewModelTrustStateMessageOrNull(
        trustState = trustState,
        isAuthenticatedUi = isAuthenticatedUi
    )

    if (trustMessage != null) {
        failClosedWithError(trustMessage, true)
        return
    }

    if (trustState == TrustState.VERIFIED && isAuthenticatedUi) {
        updateLastAction("Security trust verified.")
    }
}

internal fun handleMainViewModelSessionExpiryIfNeeded(
    alreadyNotified: Boolean,
    setSessionExpiryNotified: (Boolean) -> Unit,
    failClosedAuthentication: (String, Boolean) -> Unit
) {
    if (alreadyNotified) {
        return
    }

    setSessionExpiryNotified(true)
    failClosedAuthentication(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE, true)
}

internal fun resolveMainViewModelVaultResetFailureMessage(
    failureMessage: String?
): String {
    val normalized = failureMessage?.trim().orEmpty()

    if (normalized.isBlank()) {
        return "Vault reset failed: unknown error."
    }

    return when {
        normalized.startsWith("Vault reset denied:", ignoreCase = true) -> normalized
        normalized.startsWith("Recovery is required", ignoreCase = true) -> normalized
        normalized.startsWith("Security compromised", ignoreCase = true) -> normalized
        normalized.startsWith("Protected runtime is blocked", ignoreCase = true) -> normalized
        normalized.startsWith("Emergency limited mode", ignoreCase = true) -> normalized
        else -> "Vault reset failed: $normalized"
    }
}

internal fun completeMainViewModelVaultReset(
    result: Result<Unit>,
    applyFailClosedState: (Boolean) -> Unit,
    refreshTierAndBoundaryState: () -> Unit,
    updateLastAction: (String) -> Unit,
    setUiError: (String) -> Unit
) {
    applyFailClosedState(true)

    if (result.isSuccess) {
        refreshTierAndBoundaryState()
        updateLastAction("Vault reset complete. Fresh authentication required.")
        setUiError("Vault reset complete. Please authenticate again.")
        return
    }

    val resolvedMessage = resolveMainViewModelVaultResetFailureMessage(
        result.exceptionOrNull()?.message
    )
    updateLastAction(resolvedMessage)
    setUiError(resolvedMessage)
}

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
