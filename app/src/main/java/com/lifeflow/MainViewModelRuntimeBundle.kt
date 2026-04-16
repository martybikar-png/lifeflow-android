package com.lifeflow

import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.sync.Mutex

internal class MainViewModelRuntimeBundle(
    orchestrator: LifeFlowOrchestrator,
    trustStatePort: TrustStatePort,
    clearSession: () -> Unit,
    performVaultReset: suspend () -> Unit,
    private val currentUiState: () -> UiState,
    currentSecuritySnapshot: () -> MainViewModelSecuritySnapshot,
    setUiStateError: (String) -> Unit,
    updateLastAction: (String) -> Unit,
    private val isFreeTier: () -> Boolean,
    wipeUiCachesFailClosed: () -> Unit,
    activateFreeTierUi: () -> Unit,
    markAuthenticated: () -> Unit
) {
    val wellbeingState = MainViewModelWellbeingState()

    private val refreshMutex = Mutex()

    private val authRuntime = MainViewModelAuthRuntime(
        currentSecuritySnapshot = currentSecuritySnapshot,
        currentUiState = currentUiState
    )

    private fun shouldQueueForegroundRefresh(): Boolean {
        return !isFreeTier() && currentUiState() is UiState.Authenticated
    }

    val lifecycleRuntime = MainViewModelLifecycleRuntime(
        trustStatePort = trustStatePort,
        shouldQueueForegroundRefresh = ::shouldQueueForegroundRefresh
    )

    val authDelegate = MainViewModelAuthDelegate(
        authRuntime = authRuntime,
        clearSession = clearSession,
        wipeUiCachesFailClosed = wipeUiCachesFailClosed,
        setSessionExpiryNotified = lifecycleRuntime::setSessionExpiryNotified,
        setUiStateError = setUiStateError,
        updateLastAction = updateLastAction
    )

    private val wellbeingRuntime = MainViewModelWellbeingRuntime(
        orchestrator = orchestrator
    )

    val wellbeingDelegate = MainViewModelWellbeingDelegate(
        wellbeingRuntime = wellbeingRuntime,
        wellbeingState = wellbeingState,
        refreshMutex = refreshMutex,
        currentUiState = currentUiState,
        currentSecuritySnapshot = currentSecuritySnapshot,
        failClosedWithError = authDelegate::failClosedWithError,
        updateLastAction = updateLastAction
    )

    val actionRuntime = MainViewModelActionRuntime(
        orchestrator = orchestrator,
        wellbeingDelegate = wellbeingDelegate,
        authDelegate = authDelegate,
        performVaultReset = performVaultReset,
        updateLastAction = updateLastAction,
        isFreeTier = isFreeTier,
        isAuthenticatedUi = { currentUiState() is UiState.Authenticated },
        activateFreeTierUi = activateFreeTierUi,
        markAuthenticated = markAuthenticated
    )
}
