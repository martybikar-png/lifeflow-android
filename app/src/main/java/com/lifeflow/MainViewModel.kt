package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.launch

class MainViewModel(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit,
    private val trustStatePort: TrustStatePort,
    private val isSessionAuthorized: () -> Boolean,
    private val clearSession: () -> Unit
) : ViewModel(), ActiveRuntimeViewModelContract {

    override val uiState = mutableStateOf<UiState>(UiState.Loading)
    override val lastAction = mutableStateOf("Initializing secure LifeFlow session...")
    override val currentTier = mutableStateOf<TierState>(TierState.CORE)

    private val runtimeBundle = MainViewModelRuntimeBundle(
        orchestrator = orchestrator,
        trustStatePort = trustStatePort,
        clearSession = clearSession,
        performVaultReset = performVaultReset,
        currentUiState = { uiState.value },
        currentSecuritySnapshot = ::currentSecuritySnapshot,
        setUiStateError = { message -> uiState.value = UiState.Error(message) },
        updateLastAction = ::updateLastAction,
        isFreeTier = ::isFreeTier,
        wipeUiCachesFailClosed = ::wipeUiCachesFailClosed,
        activateFreeTierUi = ::activateFreeTierUi,
        markAuthenticated = { uiState.value = UiState.Authenticated }
    )

    private val wellbeingState = runtimeBundle.wellbeingState
    override val healthConnectState = wellbeingState.healthConnectState
    override val requiredHealthPermissions = wellbeingState.requiredHealthPermissions
    override val grantedHealthPermissions = wellbeingState.grantedHealthPermissions
    override val healthPermissionsInitError = wellbeingState.healthPermissionsInitError
    override val digitalTwinState = wellbeingState.digitalTwinState
    override val wellbeingAssessment = wellbeingState.wellbeingAssessment

    init {
        runtimeBundle.lifecycleRuntime.observeTrustState(
            scope = viewModelScope,
            authDelegate = runtimeBundle.authDelegate
        )
        runtimeBundle.lifecycleRuntime.observeSessionExpiry(
            scope = viewModelScope,
            authDelegate = runtimeBundle.authDelegate
        )
        currentTier.value = orchestrator.currentTier()

        if (isFreeTier()) {
            activateFreeTierUi()
        } else {
            runtimeBundle.wellbeingDelegate.refreshRequiredPermissionsDefinition()
        }
    }

    private fun currentSecuritySnapshot() = MainViewModelSecuritySnapshot(
        isAuthorized = isSessionAuthorized(),
        trustState = trustStatePort.currentTrustState()
    )

    private fun isFreeTier(): Boolean = currentTier.value == TierState.FREE

    private fun canExposeProtectedUiDataNow(): Boolean {
        return currentSecuritySnapshot().canExposeProtectedUiData(uiState.value)
    }

    override fun isSessionAuthorizedForUi(): Boolean = isSessionAuthorized()

    private fun updateLastAction(message: String) {
        lastAction.value = message
    }

    private fun activateFreeTierUi() {
        uiState.value = UiState.FreeTier()
        runtimeBundle.wellbeingDelegate.refreshPublicHealthStateOnly()
        updateLastAction("LifeFlow Free active. Public state ready.")
    }

    private fun wipeUiCachesFailClosed() {
        applyMainViewModelWellbeingUiUpdate(
            update = mainViewModelFailClosedUiUpdate(),
            healthConnectStateState = wellbeingState.healthConnectState,
            requiredHealthPermissionsState = wellbeingState.requiredHealthPermissions,
            grantedHealthPermissionsState = wellbeingState.grantedHealthPermissions,
            healthPermissionsInitErrorState = wellbeingState.healthPermissionsInitError,
            digitalTwinStateState = wellbeingState.digitalTwinState,
            wellbeingAssessmentState = wellbeingState.wellbeingAssessment,
            updateLastAction = ::updateLastAction
        )
    }

    override fun refreshMetricsAndTwinNow() {
        viewModelScope.launch {
            runtimeBundle.actionRuntime.triggerRuntimeRefresh("Manual dashboard refresh requested.")
        }
    }

    override fun onHealthPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            runtimeBundle.actionRuntime.triggerRuntimeRefresh(
                "Health permission result received (${granted.size} granted)."
            )
        }
    }

    override fun onAuthenticationSuccess() {
        runtimeBundle.authDelegate.beginAuthenticationSuccessFlow(
            setUiStateLoading = {}
        )
        viewModelScope.launch {
            runtimeBundle.actionRuntime.runAuthenticationBootstrap()
        }
    }

    override fun onAuthenticationError(message: String) =
        runtimeBundle.authDelegate.handleAuthenticationError(message)

    override fun onAppBackgrounded() {
        runtimeBundle.lifecycleRuntime.onAppBackgrounded()
    }

    override fun onAppForegrounded() {
        val shouldRefreshAfterRecheck = runtimeBundle.lifecycleRuntime.consumePendingForegroundRefresh()

        runtimeBundle.lifecycleRuntime.handleSessionPollTick(runtimeBundle.authDelegate)

        if (!shouldRefreshAfterRecheck) {
            return
        }

        if (canExposeProtectedUiDataNow()) {
            viewModelScope.launch {
                runtimeBundle.actionRuntime.triggerRuntimeRefresh(
                    "Returned to foreground; secure refresh requested."
                )
            }
        }
    }

    override fun resetVault() {
        runtimeBundle.authDelegate.beginVaultResetFlow(
            setUiStateLoading = {}
        )
        viewModelScope.launch {
            runtimeBundle.actionRuntime.runVaultReset()
        }
    }
}
