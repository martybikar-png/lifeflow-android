package com.lifeflow

import com.lifeflow.domain.security.TrustState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

internal fun initializeMainViewModelCoordinator(
    scope: CoroutineScope,
    trustStatePort: TrustStatePort,
    sessionPollMs: Long,
    isSessionExpiryNotified: () -> Boolean,
    currentSecurityEvaluation: () -> MainViewModelSecurityEvaluation,
    onTrustStateObserved: (TrustState) -> Unit,
    setSessionExpiryNotified: (Boolean) -> Unit,
    failClosedAuthentication: (String, Boolean) -> Unit
) {
    observeMainViewModelTrustState(
        scope = scope,
        trustStatePort = trustStatePort,
        onTrustStateObserved = onTrustStateObserved
    )

    observeMainViewModelSessionExpiry(
        scope = scope,
        sessionPollMs = sessionPollMs,
        isSessionExpiryNotified = isSessionExpiryNotified,
        onSessionPollTick = { alreadyNotified ->
            handleMainViewModelSessionPollTick(
                securityEvaluation = currentSecurityEvaluation(),
                alreadyNotified = alreadyNotified,
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
        }
    )
}

internal suspend fun refreshMainViewModelProtectedSnapshotFromCoordinator(
    identityInitialized: Boolean,
    refreshMutex: Mutex,
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    canExposeProtectedUiDataNow: () -> Boolean,
    isAuthenticatedUiNow: () -> Boolean,
    updateLastAction: (String) -> Unit,
    failClosedWithError: (String, Boolean) -> Unit
) {
    refreshMainViewModelProtectedSnapshot(
        identityInitialized = identityInitialized,
        refreshWellbeingSnapshotSafe = { initialized ->
            refreshMainViewModelWellbeingSnapshotSafe(
                identityInitialized = initialized,
                refreshMutex = refreshMutex,
                wellbeingRuntime = wellbeingRuntime,
                wellbeingState = wellbeingState,
                canExposeProtectedUiDataNow = canExposeProtectedUiDataNow,
                updateLastAction = updateLastAction,
                refreshPublicHealthStateWithMessage = { message ->
                    refreshMainViewModelPublicHealthStateWithMessage(
                        message = message,
                        refreshPublicHealthStateOnly = {
                            refreshMainViewModelPublicHealthStateOnly(
                                wellbeingRuntime = wellbeingRuntime,
                                wellbeingState = wellbeingState,
                                updateLastAction = updateLastAction
                            )
                        },
                        updateLastAction = updateLastAction
                    )
                },
                failClosedWithError = failClosedWithError
            )
        },
        handleUnexpectedProtectedRefreshFailure = {
            handleMainViewModelUnexpectedProtectedRefreshFailure(
                wasAuthenticated = isAuthenticatedUiNow(),
                refreshPublicHealthStateOnly = {
                    refreshMainViewModelPublicHealthStateOnly(
                        wellbeingRuntime = wellbeingRuntime,
                        wellbeingState = wellbeingState,
                        updateLastAction = updateLastAction
                    )
                },
                failClosedWithError = failClosedWithError,
                updateLastAction = updateLastAction
            )
        }
    )
}

internal fun launchMainViewModelRuntimeRefresh(
    scope: CoroutineScope,
    lastActionMessage: String,
    refreshTierAndBoundaryState: () -> Unit,
    isFreeTier: () -> Boolean,
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    refreshMutex: Mutex,
    canExposeProtectedUiDataNow: () -> Boolean,
    isAuthenticatedUiNow: () -> Boolean,
    updateLastAction: (String) -> Unit,
    failClosedWithError: (String, Boolean) -> Unit
) {
    scope.launch {
        triggerMainViewModelRuntimeRefresh(
            lastActionMessage = lastActionMessage,
            updateLastAction = updateLastAction,
            refreshTierAndBoundaryState = refreshTierAndBoundaryState,
            isFreeTier = isFreeTier,
            refreshPublicHealthStateOnly = {
                refreshMainViewModelPublicHealthStateOnly(
                    wellbeingRuntime = wellbeingRuntime,
                    wellbeingState = wellbeingState,
                    updateLastAction = updateLastAction
                )
            },
            refreshProtectedSnapshot = { identityInitialized ->
                refreshMainViewModelProtectedSnapshotFromCoordinator(
                    identityInitialized = identityInitialized,
                    refreshMutex = refreshMutex,
                    wellbeingRuntime = wellbeingRuntime,
                    wellbeingState = wellbeingState,
                    canExposeProtectedUiDataNow = canExposeProtectedUiDataNow,
                    isAuthenticatedUiNow = isAuthenticatedUiNow,
                    updateLastAction = updateLastAction,
                    failClosedWithError = failClosedWithError
                )
            },
            isAuthenticatedUiNow = isAuthenticatedUiNow
        )
    }
}
