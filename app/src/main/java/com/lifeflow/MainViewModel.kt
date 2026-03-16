package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.core.ActionResult
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.core.WellbeingRefreshSnapshot
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainViewModel(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit
) : ViewModel() {
    var uiState = mutableStateOf<UiState>(UiState.Loading)
        private set
    var lastAction = mutableStateOf("Initializing secure LifeFlow session...")
        private set
    var healthConnectState = mutableStateOf<HealthConnectUiState>(HealthConnectUiState.Unknown)
        private set
    var requiredHealthPermissions = mutableStateOf<Set<String>>(emptySet())
        private set
    var grantedHealthPermissions = mutableStateOf<Set<String>>(emptySet())
        private set
    var healthPermissionsInitError = mutableStateOf<String?>(null)
        private set
    var digitalTwinState = mutableStateOf<DigitalTwinState?>(null)
        private set

    private var sessionExpiryNotified = false
    private val sessionPollMs = 1000L
    private val refreshMutex = Mutex()

    init {
        observeTrustState()
        observeSessionExpiry()
        refreshRequiredPermissionsDefinition()
    }

    private fun updateLastAction(message: String) {
        lastAction.value = message
    }

    private fun wipeUiCachesFailClosed() {
        grantedHealthPermissions.value = emptySet()
        digitalTwinState.value = null
        updateLastAction("Protected dashboard data cleared.")
    }

    private fun refreshPublicHealthStateOnly() {
        refreshHealthConnectStatusSafe()
        refreshRequiredPermissionsDefinition()
        grantedHealthPermissions.value = emptySet()
        digitalTwinState.value = null
        updateLastAction("Public Health Connect state refreshed.")
    }

    private fun failClosedWithError(
        message: String,
        clearSession: Boolean = true
    ) {
        if (clearSession) {
            SecurityAccessSession.clear()
        }
        wipeUiCachesFailClosed()
        sessionExpiryNotified = false
        uiState.value = UiState.Error(message)
        updateLastAction("Fail-closed: $message")
    }

    private fun ensureProtectedEntryAllowed(): Boolean {
        val blockedMessage = mainViewModelProtectedEntryBlockMessage(
            isAuthorized = SecurityAccessSession.isAuthorized(),
            trustState = SecurityRuleEngine.getTrustState()
        )

        if (blockedMessage != null) {
            failClosedWithError(blockedMessage)
            return false
        }

        updateLastAction("Authentication verified. Bootstrapping identity...")
        return true
    }

    private fun refreshRequiredPermissionsDefinition() {
        when (val res = orchestrator.requiredHealthPermissionsSafe()) {
            is ActionResult.Success -> {
                requiredHealthPermissions.value = res.value
                healthPermissionsInitError.value = null
                updateLastAction(
                    "Health permission contract loaded (${res.value.size} required)."
                )
            }

            is ActionResult.Error -> {
                requiredHealthPermissions.value = emptySet()
                healthPermissionsInitError.value = res.message
                updateLastAction("Health permission contract failed to load.")
            }

            is ActionResult.Locked -> {
                requiredHealthPermissions.value = emptySet()
                healthPermissionsInitError.value = res.reason
                updateLastAction("Health permission contract locked.")
            }
        }
    }

    private fun refreshHealthConnectStatusSafe() {
        healthConnectState.value = runCatching {
            orchestrator.healthConnectUiState()
        }.getOrElse {
            HealthConnectUiState.Unknown
        }
        updateLastAction("Health Connect state checked: ${healthConnectState.value}.")
    }

    private fun handleUnexpectedProtectedRefreshFailure() {
        refreshPublicHealthStateOnly()

        if (uiState.value is UiState.Authenticated) {
            failClosedWithError(
                message = MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE,
                clearSession = true
            )
        } else {
            updateLastAction("Protected refresh failed unexpectedly.")
        }
    }

    private fun applyWellbeingSnapshot(snapshot: WellbeingRefreshSnapshot) {
        healthConnectState.value = snapshot.healthConnectState
        requiredHealthPermissions.value = snapshot.requiredPermissions
        healthPermissionsInitError.value = null

        if (canMainViewModelExposeProtectedUiData(uiState.value)) {
            grantedHealthPermissions.value = snapshot.grantedPermissions
            digitalTwinState.value = snapshot.digitalTwinState
            updateLastAction(
                "Dashboard snapshot updated (${snapshot.grantedPermissions.size}/${snapshot.requiredPermissions.size} permissions granted)."
            )
        } else {
            grantedHealthPermissions.value = emptySet()
            digitalTwinState.value = null
            updateLastAction("Protected snapshot received but UI remained fail-closed.")
        }
    }

    private fun observeTrustState() {
        viewModelScope.launch {
            SecurityRuleEngine.trustState.collect { state ->
                when (val update = resolveMainViewModelTrustUpdate(state, uiState.value)) {
                    is MainViewModelTrustUpdate.FailClosed -> {
                        failClosedWithError(update.message)
                    }

                    is MainViewModelTrustUpdate.LastAction -> {
                        updateLastAction(update.message)
                    }

                    MainViewModelTrustUpdate.NoOp -> Unit
                }
            }
        }
    }

    private fun observeSessionExpiry() {
        viewModelScope.launch {
            while (isActive) {
                delay(sessionPollMs)

                if (shouldMainViewModelExpireSession(uiState.value)) {
                    if (!sessionExpiryNotified) {
                        sessionExpiryNotified = true
                        failClosedWithError(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE)
                    }
                } else {
                    sessionExpiryNotified = false
                }
            }
        }
    }

    private suspend fun refreshWellbeingSnapshotSafe(identityInitialized: Boolean) {
        refreshMutex.withLock {
            if (!canMainViewModelExposeProtectedUiData(uiState.value)) {
                refreshPublicHealthStateOnly()
                updateLastAction(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE)
                return
            }

            updateLastAction("Refreshing protected wellbeing snapshot...")

            when (val res = orchestrator.refreshWellbeingSnapshot(identityInitialized)) {
                is ActionResult.Success -> {
                    applyWellbeingSnapshot(res.value)
                }

                is ActionResult.Error -> {
                    refreshPublicHealthStateOnly()
                    updateLastAction("Protected refresh failed. Public state kept available.")
                }

                is ActionResult.Locked -> {
                    failClosedWithError(
                        message = mainViewModelLockedReasonToUserMessage(res.reason),
                        clearSession = true
                    )
                }
            }
        }
    }

    private fun requestProtectedRefresh() {
        viewModelScope.launch {
            runCatching {
                refreshWellbeingSnapshotSafe(
                    identityInitialized = uiState.value is UiState.Authenticated
                )
            }.onFailure {
                handleUnexpectedProtectedRefreshFailure()
            }
        }
    }

    fun refreshMetricsAndTwinNow() {
        updateLastAction("Manual dashboard refresh requested.")
        requestProtectedRefresh()
    }

    fun onHealthPermissionsResult(granted: Set<String>) {
        updateLastAction("Health permission result received (${granted.size} granted).")
        requestProtectedRefresh()
    }

    fun onAuthenticationSuccess() {
        uiState.value = UiState.Loading
        updateLastAction("Authentication succeeded. Preparing protected dashboard...")

        viewModelScope.launch {
            if (!ensureProtectedEntryAllowed()) {
                return@launch
            }

            sessionExpiryNotified = false

            when (val boot = orchestrator.bootstrapIdentityIfNeeded()) {
                is ActionResult.Success -> {
                    uiState.value = UiState.Authenticated
                    updateLastAction("Protected dashboard unlocked.")
                }

                is ActionResult.Locked -> {
                    failClosedWithError(
                        message = mainViewModelLockedReasonToUserMessage(boot.reason),
                        clearSession = true
                    )
                }

                is ActionResult.Error -> {
                    failClosedWithError(boot.message)
                }
            }
        }
    }

    fun onAuthenticationError(message: String) {
        failClosedWithError(message)
    }

    fun resetVault() {
        uiState.value = UiState.Loading
        updateLastAction("Vault reset requested.")

        viewModelScope.launch {
            val result = runCatching { performVaultReset() }
            SecurityAccessSession.clear()
            wipeUiCachesFailClosed()
            sessionExpiryNotified = false

            uiState.value = if (result.isSuccess) {
                updateLastAction("Vault reset complete. Fresh authentication required.")
                UiState.Error("Vault reset complete. Please authenticate again.")
            } else {
                updateLastAction("Vault reset failed.")
                UiState.Error(
                    "Vault reset failed: ${result.exceptionOrNull()?.message ?: "unknown"}"
                )
            }
        }
    }
}