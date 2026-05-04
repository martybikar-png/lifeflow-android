package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.boundary.BoundaryAccessController
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.security.TrustState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.sync.Mutex

class MainViewModel(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit,
    private val trustStatePort: TrustStatePort,
    private val isSessionAuthorized: () -> Boolean,
    private val clearSession: () -> Unit
) : ViewModel(), ActiveRuntimeViewModelContract {

    override val uiState = mutableStateOf<UiState>(UiState.Loading)
    override val lastAction = mutableStateOf("Initializing secure LifeFlow session...")
    override val freeTierMessage = mutableStateOf("Free tier active.")
    override val boundarySnapshot = mutableStateOf(MainBoundarySnapshot.initial())
    override val quickCaptureLibrary = mutableStateOf(QuickCaptureLibraryPresentation.initial())
    private val currentTier = mutableStateOf<TierState>(TierState.CORE)
    private val wellbeingState = MainViewModelWellbeingState()
    private val refreshMutex = Mutex()
    private val boundaryAccessController = BoundaryAccessController()
    private val wellbeingRuntime = MainViewModelWellbeingRuntime(
        orchestrator = orchestrator
    )
    private val captureGateway = MainViewModelCaptureGateway(
        scope = viewModelScope,
        orchestrator = orchestrator,
        quickCaptureLibrary = quickCaptureLibrary,
        canPerformProtectedWriteNow = { currentSecurityEvaluation().canPerformProtectedWrite },
        canExposeProtectedUiDataNow = ::canExposeProtectedUiDataNow,
        failClosedWithError = ::failClosedWithError,
        updateLastAction = ::updateLastAction
    )

    private var sessionExpiryNotified = false
    private var pendingForegroundRefresh = false
    private val sessionPollMs = 1000L
    private val activeRuntimeGateway = MainViewModelActiveRuntimeGateway(
        scope = viewModelScope,
        beginAuthenticationSuccessFlow = ::beginAuthenticationSuccessFlow,
        runAuthenticationBootstrap = ::runAuthenticationBootstrap,
        failClosedAuthentication = ::failClosedAuthentication,
        isFreeTier = ::isFreeTier,
        isAuthenticatedUiNow = ::isAuthenticatedUiNow,
        pendingForegroundRefresh = { pendingForegroundRefresh },
        updatePendingForegroundRefresh = { pendingForegroundRefresh = it },
        currentSecurityEvaluation = ::currentSecurityEvaluation,
        isSessionExpiryNotified = { sessionExpiryNotified },
        setSessionExpiryNotified = ::setSessionExpiryNotified,
        canExposeProtectedUiDataNow = ::canExposeProtectedUiDataNow,
        launchRuntimeRefresh = ::launchRuntimeRefresh,
        runVaultReset = ::runVaultReset,
        updateGrantedHealthPermissions = { wellbeingState.grantedHealthPermissions.value = it }
    )

    override val healthConnectState = wellbeingState.healthConnectState
    override val requiredHealthPermissions = wellbeingState.requiredHealthPermissions
    override val grantedHealthPermissions = wellbeingState.grantedHealthPermissions
    override val healthPermissionsInitError = wellbeingState.healthPermissionsInitError
    override val digitalTwinState = wellbeingState.digitalTwinState
    override val wellbeingAssessment = wellbeingState.wellbeingAssessment

    init {
        initializeMainViewModelCoordinator(
            scope = viewModelScope,
            trustStatePort = trustStatePort,
            sessionPollMs = sessionPollMs,
            isSessionExpiryNotified = { sessionExpiryNotified },
            currentSecurityEvaluation = ::currentSecurityEvaluation,
            onTrustStateObserved = ::handleObservedTrustState,
            setSessionExpiryNotified = ::setSessionExpiryNotified,
            failClosedAuthentication = ::failClosedAuthentication
        )

        refreshTierAndBoundaryState()

        if (isFreeTier()) {
            activateFreeTierUi()
        } else {
            refreshMainViewModelRequiredPermissionsDefinition(
                wellbeingRuntime = wellbeingRuntime,
                wellbeingState = wellbeingState,
                updateLastAction = ::updateLastAction
            )
        }
    }

    private fun isAuthenticatedUiNow(): Boolean =
        uiState.value is UiState.Authenticated

    private fun currentSecurityEvaluation(): MainViewModelSecurityEvaluation =
        mainViewModelSecurityEvaluation(
            isAuthenticatedUi = isAuthenticatedUiNow(),
            isAuthorized = isSessionAuthorized(),
            trustState = trustStatePort.currentTrustState()
        )


    private fun setSessionExpiryNotified(value: Boolean) { sessionExpiryNotified = value }

    private fun isFreeTier(): Boolean = currentTier.value == TierState.FREE

    private fun canExposeProtectedUiDataNow(): Boolean =
        currentSecurityEvaluation().canExposeProtectedUiData

    override fun isSessionAuthorizedForUi(): Boolean = isSessionAuthorized()

    private fun updateLastAction(message: String) { lastAction.value = message }

    private fun setUiError(message: String) { uiState.value = UiState.Error(message) }

    private fun refreshTierAndBoundaryState(): MainViewModelBoundaryRefreshResult {
        val refreshed = refreshMainViewModelBoundaryState(
            orchestrator = orchestrator,
            boundaryAccessController = boundaryAccessController
        )

        currentTier.value = refreshed.currentTier
        boundarySnapshot.value = refreshed.boundarySnapshot
        freeTierMessage.value = refreshed.freeTierMessage

        return refreshed
    }

    private fun activateFreeTierUi() {
        val refreshed = refreshTierAndBoundaryState()
        uiState.value = UiState.FreeTier
        refreshMainViewModelPublicHealthStateOnly(
            wellbeingRuntime = wellbeingRuntime,
            wellbeingState = wellbeingState,
            updateLastAction = ::updateLastAction
        )

        updateLastAction(
            if (refreshed.blockedCount > 0) {
                "LifeFlow Free active. ${refreshed.blockedCount} Core surfaces unavailable."
            } else {
                "LifeFlow Free active. Public state ready."
            }
        )
    }

    private fun wipeUiCachesFailClosed() {
        wipeMainViewModelUiCachesFailClosed(
            wellbeingState = wellbeingState,
            updateLastAction = ::updateLastAction
        )
    }

    private fun applyFailClosedState(clearSession: Boolean) {
        applyMainViewModelFailClosedState(
            clearSession = clearSession,
            clearSessionAction = { this.clearSession() },
            wipeUiCachesFailClosed = ::wipeUiCachesFailClosed,
            setSessionExpiryNotified = ::setSessionExpiryNotified
        )
    }

    private fun failClosedAuthentication(message: String, clearSession: Boolean = true) = failClosedWithError(message, clearSession)

    private fun failClosedWithError(
        message: String,
        clearSession: Boolean = true
    ) {
        failClosedMainViewModelWithError(
            message = message,
            clearSession = clearSession,
            applyFailClosedState = ::applyFailClosedState,
            setUiError = ::setUiError,
            updateLastAction = ::updateLastAction
        )
    }

    private fun ensureRuntimeEntryAllowed(): Boolean {
        return ensureMainViewModelRuntimeEntryAllowed(
            securityEvaluation = currentSecurityEvaluation(),
            updateLastAction = ::updateLastAction,
            failClosedAuthentication = ::failClosedAuthentication
        )
    }

    private fun beginAuthenticationSuccessFlow(setUiStateLoading: () -> Unit) {
        setUiStateLoading()
        updateLastAction("Authentication succeeded. Preparing protected dashboard...")
    }

    private fun handleObservedTrustState(trustState: TrustState) {
        handleMainViewModelObservedTrustState(
            trustState = trustState,
            isAuthenticatedUi = isAuthenticatedUiNow(),
            failClosedWithError = ::failClosedWithError,
            updateLastAction = ::updateLastAction
        )
    }

    private fun handleAuthenticationError(message: String) {
        failClosedAuthentication(message)
    }

    private fun completeVaultReset(result: Result<Unit>) {
        completeMainViewModelVaultReset(
            result = result,
            applyFailClosedState = ::applyFailClosedState,
            refreshTierAndBoundaryState = { refreshTierAndBoundaryState() },
            updateLastAction = ::updateLastAction,
            setUiError = ::setUiError
        )
    }

    private suspend fun runAuthenticationBootstrap() {
        runMainViewModelAuthenticationBootstrap(
            orchestrator = orchestrator,
            refreshTierAndBoundaryState = { refreshTierAndBoundaryState() },
            isFreeTier = ::isFreeTier,
            clearSessionExpiryNotification = { setSessionExpiryNotified(false) },
            activateFreeTierUi = ::activateFreeTierUi,
            ensureRuntimeEntryAllowed = ::ensureRuntimeEntryAllowed,
            markAuthenticated = { uiState.value = UiState.Authenticated },
            onLocked = { message -> failClosedAuthentication(message) },
            onError = { message -> failClosedAuthentication(message) },
            updateLastAction = ::updateLastAction
        )
    }

    private suspend fun runVaultReset() {
        completeVaultReset(
            result = runCatching { performVaultReset() }
        )
    }

    private fun launchRuntimeRefresh(lastActionMessage: String) {
        launchMainViewModelRuntimeRefresh(
            scope = viewModelScope,
            lastActionMessage = lastActionMessage,
            refreshTierAndBoundaryState = { refreshTierAndBoundaryState() },
            isFreeTier = ::isFreeTier,
            wellbeingRuntime = wellbeingRuntime,
            wellbeingState = wellbeingState,
            refreshMutex = refreshMutex,
            canExposeProtectedUiDataNow = ::canExposeProtectedUiDataNow,
            isAuthenticatedUiNow = ::isAuthenticatedUiNow,
            updateLastAction = ::updateLastAction,
            failClosedWithError = ::failClosedWithError
        )
    }

    override fun refreshMetricsAndTwinNow() =
        launchRuntimeRefresh("Manual dashboard refresh requested.")

    override fun saveQuickCaptureDraft(note: String) =
        captureGateway.saveQuickCaptureDraft(note)

    override fun loadQuickCaptureLibrary() =
        captureGateway.loadQuickCaptureLibrary()

    override fun deleteQuickCapture(id: String) =
        captureGateway.deleteQuickCapture(id)

    override fun updateQuickCapture(id: String, note: String) =
        captureGateway.updateQuickCapture(id, note)

    override fun onHealthPermissionsResult(granted: Set<String>) =
        activeRuntimeGateway.onHealthPermissionsResult(granted)

    override fun onAuthenticationSuccess() =
        activeRuntimeGateway.onAuthenticationSuccess()

    override fun onAuthenticationError(message: String) =
        activeRuntimeGateway.onAuthenticationError(message)

    override fun onAppBackgrounded() =
        activeRuntimeGateway.onAppBackgrounded()

    override fun onAppForegrounded() =
        activeRuntimeGateway.onAppForegrounded()

    override fun resetVault() =
        activeRuntimeGateway.resetVault()
}
