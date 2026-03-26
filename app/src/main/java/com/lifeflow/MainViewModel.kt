package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.core.ActionResult
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.security.TrustStatePort
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class MainViewModel(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit,
    private val trustStatePort: TrustStatePort,
    private val isSessionAuthorized: () -> Boolean,
    private val clearSession: () -> Unit
) : ViewModel() {

    val uiState = mutableStateOf<UiState>(UiState.Loading)
    val lastAction = mutableStateOf("Initializing secure LifeFlow session...")
    val healthConnectState = mutableStateOf<HealthConnectUiState>(HealthConnectUiState.Unknown)
    val requiredHealthPermissions = mutableStateOf<Set<String>>(emptySet())
    val grantedHealthPermissions = mutableStateOf<Set<String>>(emptySet())
    val healthPermissionsInitError = mutableStateOf<String?>(null)
    val digitalTwinState = mutableStateOf<DigitalTwinState?>(null)
    val wellbeingAssessment = mutableStateOf<WellbeingAssessment?>(null)
    val currentTier = mutableStateOf<TierState>(TierState.CORE)

    private var sessionExpiryNotified = false
    private var pendingForegroundRefresh = false
    private val sessionPollMs = 1000L
    private val refreshMutex = Mutex()

    private val authDelegate = MainViewModelAuthDelegate(
        currentSecuritySnapshot = ::currentSecuritySnapshot,
        clearSession = clearSession,
        wipeUiCachesFailClosed = ::wipeUiCachesFailClosed,
        setSessionExpiryNotified = { sessionExpiryNotified = it },
        setUiStateError = { message -> uiState.value = UiState.Error(message) },
        updateLastAction = ::updateLastAction,
        currentUiState = { uiState.value }
    )

    private val wellbeingDelegate = MainViewModelWellbeingDelegate(
        orchestrator = orchestrator,
        healthConnectState = healthConnectState,
        requiredHealthPermissions = requiredHealthPermissions,
        grantedHealthPermissions = grantedHealthPermissions,
        healthPermissionsInitError = healthPermissionsInitError,
        digitalTwinState = digitalTwinState,
        wellbeingAssessment = wellbeingAssessment,
        refreshMutex = refreshMutex,
        currentUiState = { uiState.value },
        currentSecuritySnapshot = ::currentSecuritySnapshot,
        failClosedWithError = authDelegate::failClosedWithError,
        updateLastAction = ::updateLastAction
    )

    init {
        observeTrustState()
        observeSessionExpiry()
        currentTier.value = orchestrator.currentTier()

        if (isFreeTier()) {
            activateFreeTierUi()
        } else {
            wellbeingDelegate.refreshRequiredPermissionsDefinition()
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

    private fun shouldQueueForegroundRefresh(): Boolean {
        return !isFreeTier() && uiState.value is UiState.Authenticated
    }

    internal fun isSessionAuthorizedForUi(): Boolean = isSessionAuthorized()

    private fun updateLastAction(message: String) {
        lastAction.value = message
    }

    private fun activateFreeTierUi() {
        uiState.value = UiState.FreeTier()
        wellbeingDelegate.refreshPublicHealthStateOnly()
        updateLastAction("LifeFlow Free active. Public state ready.")
    }

    private fun triggerRuntimeRefresh(lastActionMessage: String) {
        updateLastAction(lastActionMessage)

        if (isFreeTier()) {
            wellbeingDelegate.refreshPublicHealthStateOnly()
            return
        }

        requestProtectedRefresh(
            identityInitialized = uiState.value is UiState.Authenticated
        )
    }

    private fun wipeUiCachesFailClosed() {
        applyMainViewModelWellbeingUiUpdate(
            update = mainViewModelFailClosedUiUpdate(),
            healthConnectStateState = healthConnectState,
            requiredHealthPermissionsState = requiredHealthPermissions,
            grantedHealthPermissionsState = grantedHealthPermissions,
            healthPermissionsInitErrorState = healthPermissionsInitError,
            digitalTwinStateState = digitalTwinState,
            wellbeingAssessmentState = wellbeingAssessment,
            updateLastAction = ::updateLastAction
        )
    }

    private fun observeTrustState() {
        viewModelScope.launch {
            trustStatePort.observeTrustState().collect { state ->
                authDelegate.handleObservedTrustState(state)
            }
        }
    }

    private fun observeSessionExpiry() {
        viewModelScope.launch {
            while (isActive) {
                delay(sessionPollMs)
                authDelegate.handleSessionPollTick(sessionExpiryNotified)
            }
        }
    }

    private fun requestProtectedRefresh(identityInitialized: Boolean) {
        viewModelScope.launch {
            runCatching {
                wellbeingDelegate.refreshWellbeingSnapshotSafe(
                    identityInitialized = identityInitialized
                )
            }.onFailure {
                wellbeingDelegate.handleUnexpectedProtectedRefreshFailure()
            }
        }
    }

    private suspend fun runAuthenticationBootstrap() {
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
                    markAuthenticated = { uiState.value = UiState.Authenticated }
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

    private suspend fun runVaultReset() {
        val result = runCatching { performVaultReset() }
        authDelegate.completeVaultReset(
            isSuccess = result.isSuccess,
            failureMessage = result.exceptionOrNull()?.message
        )
    }

    fun refreshMetricsAndTwinNow() =
        triggerRuntimeRefresh("Manual dashboard refresh requested.")

    fun onHealthPermissionsResult(granted: Set<String>) =
        triggerRuntimeRefresh("Health permission result received (${granted.size} granted).")

    fun onAuthenticationSuccess() {
        authDelegate.beginAuthenticationSuccessFlow(
            setUiStateLoading = {}
        )
        viewModelScope.launch {
            runAuthenticationBootstrap()
        }
    }

    fun onAuthenticationError(message: String) =
        authDelegate.handleAuthenticationError(message)

    fun onAppBackgrounded() {
        pendingForegroundRefresh = shouldQueueForegroundRefresh()
    }

    fun onAppForegrounded() {
        val shouldRefreshAfterRecheck = pendingForegroundRefresh
        pendingForegroundRefresh = false

        authDelegate.handleSessionPollTick(sessionExpiryNotified)

        if (!shouldRefreshAfterRecheck) {
            return
        }

        if (canExposeProtectedUiDataNow()) {
            triggerRuntimeRefresh("Returned to foreground; secure refresh requested.")
        }
    }

    fun resetVault() {
        authDelegate.beginVaultResetFlow(
            setUiStateLoading = {}
        )
        viewModelScope.launch {
            runVaultReset()
        }
    }
}
