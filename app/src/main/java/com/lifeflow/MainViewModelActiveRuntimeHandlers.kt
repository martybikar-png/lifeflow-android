package com.lifeflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun handleMainViewModelHealthPermissionsResult(
    granted: Set<String>,
    updateGrantedHealthPermissions: (Set<String>) -> Unit,
    launchRuntimeRefresh: (String) -> Unit
) {
    updateGrantedHealthPermissions(granted)
    launchRuntimeRefresh("Health permission result received (${granted.size} granted).")
}

internal fun launchMainViewModelAuthenticationSuccess(
    scope: CoroutineScope,
    beginAuthenticationSuccessFlow: (() -> Unit) -> Unit,
    runAuthenticationBootstrap: suspend () -> Unit
) {
    beginAuthenticationSuccessFlow {}
    scope.launch {
        runAuthenticationBootstrap()
    }
}

internal fun handleMainViewModelAuthenticationError(
    message: String,
    failClosedAuthentication: (String, Boolean) -> Unit
) {
    failClosedAuthentication(message, true)
}

internal fun handleMainViewModelAppBackgrounded(
    isFreeTier: () -> Boolean,
    isAuthenticatedUiNow: () -> Boolean,
    updatePendingForegroundRefresh: (Boolean) -> Unit
) {
    updatePendingForegroundRefresh(!isFreeTier() && isAuthenticatedUiNow())
}

internal fun handleMainViewModelAppForegrounded(
    pendingForegroundRefresh: Boolean,
    updatePendingForegroundRefresh: (Boolean) -> Unit,
    currentSecurityEvaluation: () -> MainViewModelSecurityEvaluation,
    isSessionExpiryNotified: () -> Boolean,
    setSessionExpiryNotified: (Boolean) -> Unit,
    failClosedAuthentication: (String, Boolean) -> Unit,
    canExposeProtectedUiDataNow: () -> Boolean,
    launchRuntimeRefresh: (String) -> Unit
) {
    val shouldRefreshAfterRecheck = consumeMainViewModelPendingForegroundRefresh(
        pendingForegroundRefresh = pendingForegroundRefresh,
        updatePendingForegroundRefresh = updatePendingForegroundRefresh
    )

    handleMainViewModelSessionPollTick(
        securityEvaluation = currentSecurityEvaluation(),
        alreadyNotified = isSessionExpiryNotified(),
        handleSessionExpiryIfNeeded = { notified ->
            handleMainViewModelSessionExpiryIfNeeded(
                alreadyNotified = notified,
                setSessionExpiryNotified = setSessionExpiryNotified,
                failClosedAuthentication = failClosedAuthentication
            )
        },
        clearSessionExpiryNotification = {
            setSessionExpiryNotified(false)
        }
    )

    if (!shouldRefreshAfterRecheck) {
        return
    }

    if (canExposeProtectedUiDataNow()) {
        launchRuntimeRefresh("Returned to foreground; secure refresh requested.")
    }
}

internal fun launchMainViewModelVaultReset(
    scope: CoroutineScope,
    beginAuthenticationSuccessFlow: (() -> Unit) -> Unit,
    runVaultReset: suspend () -> Unit
) {
    beginAuthenticationSuccessFlow {}
    scope.launch {
        runVaultReset()
    }
}
