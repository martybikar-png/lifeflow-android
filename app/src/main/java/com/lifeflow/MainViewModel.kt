package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
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

    // Health Connect status
    var healthConnectState = mutableStateOf<HealthConnectUiState>(HealthConnectUiState.Unknown)
        private set

    // Permissions
    var requiredHealthPermissions = mutableStateOf<Set<String>>(emptySet())
        private set

    var grantedHealthPermissions = mutableStateOf<Set<String>>(emptySet())
        private set

    // Visible reason if permissions init fails (never silent)
    var healthPermissionsInitError = mutableStateOf<String?>(null)
        private set

    // Digital Twin state exposed to UI
    var digitalTwinState = mutableStateOf<DigitalTwinState?>(null)
        private set

    // --- Phase B1/B2: session TTL / auto-expiry + trust-state reactions ---
    private var sessionExpiryNotified = false
    private val SESSION_POLL_MS = 1000L

    // Serialize protected refreshes so startup/auth/settings/manual refreshes do not overlap.
    private val refreshMutex = Mutex()

    private fun wipeUiCachesFailClosed() {
        // keep requiredHealthPermissions (static), wipe granted + twin (dynamic)
        grantedHealthPermissions.value = emptySet()
        digitalTwinState.value = null
    }

    private fun refreshPublicHealthStateOnly() {
        refreshHealthConnectStatusSafe()
        refreshRequiredPermissionsDefinition()
        grantedHealthPermissions.value = emptySet()
        digitalTwinState.value = null
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
    }

    private fun lockedReasonToUserMessage(reason: String): String {
        return when {
            reason.startsWith("COMPROMISED:", ignoreCase = true) ->
                "Security compromised. Reset vault is required before continuing."

            reason.startsWith("AUTH_REQUIRED:", ignoreCase = true) ->
                "Authentication required. Please authenticate again."

            reason.isBlank() ->
                "Access locked. Please authenticate again."

            else -> reason
        }
    }

    private fun handleLockedProtectedOperation(reason: String) {
        failClosedWithError(
            message = lockedReasonToUserMessage(reason),
            clearSession = true
        )
    }

    private fun canExposeProtectedUiData(): Boolean {
        return uiState.value is UiState.Authenticated &&
                SecurityAccessSession.isAuthorized() &&
                SecurityRuleEngine.getTrustState() == TrustState.VERIFIED
    }

    private fun refreshRequiredPermissionsDefinition() {
        when (val res = orchestrator.requiredHealthPermissionsSafe()) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                requiredHealthPermissions.value = res.value
                healthPermissionsInitError.value = null
            }

            is LifeFlowOrchestrator.ActionResult.Error -> {
                requiredHealthPermissions.value = emptySet()
                healthPermissionsInitError.value = res.message
            }

            is LifeFlowOrchestrator.ActionResult.Locked -> {
                requiredHealthPermissions.value = emptySet()
                healthPermissionsInitError.value = res.reason
            }
        }
    }

    private fun refreshHealthConnectStatusSafe() {
        healthConnectState.value = runCatching {
            orchestrator.healthConnectUiState()
        }.getOrElse {
            HealthConnectUiState.Unknown
        }
    }

    private fun handleUnexpectedProtectedRefreshFailure() {
        refreshPublicHealthStateOnly()

        if (uiState.value is UiState.Authenticated) {
            failClosedWithError(
                message = "Protected refresh failed unexpectedly. Please authenticate again.",
                clearSession = true
            )
        }
    }

    private fun applyWellbeingSnapshot(
        snapshot: LifeFlowOrchestrator.WellbeingRefreshSnapshot
    ) {
        healthConnectState.value = snapshot.healthConnectState
        requiredHealthPermissions.value = snapshot.requiredPermissions
        healthPermissionsInitError.value = null

        if (canExposeProtectedUiData()) {
            grantedHealthPermissions.value = snapshot.grantedPermissions
            digitalTwinState.value = snapshot.digitalTwinState
        } else {
            grantedHealthPermissions.value = emptySet()
            digitalTwinState.value = null
        }
    }

    init {
        // B2: Fail-closed reaction to TrustState changes
        viewModelScope.launch {
            SecurityRuleEngine.trustState.collect { state ->
                when (state) {
                    TrustState.COMPROMISED -> {
                        failClosedWithError(
                            "Security compromised. Reset vault is required before continuing."
                        )
                    }

                    TrustState.DEGRADED -> {
                        // React only if user was already authenticated and got downgraded.
                        if (uiState.value is UiState.Authenticated) {
                            failClosedWithError("Security degraded. Please authenticate again.")
                        }
                    }

                    TrustState.VERIFIED -> {
                        // no-op
                    }
                }
            }
        }

        // B1: detect TTL expiry while app is running (fail-closed UX)
        viewModelScope.launch {
            while (isActive) {
                delay(SESSION_POLL_MS)

                if (uiState.value is UiState.Authenticated) {
                    val authorized = SecurityAccessSession.isAuthorized()
                    if (!authorized) {
                        if (!sessionExpiryNotified) {
                            sessionExpiryNotified = true
                            failClosedWithError("Session expired. Please authenticate again.")
                        }
                    } else {
                        sessionExpiryNotified = false
                    }
                } else {
                    sessionExpiryNotified = false
                }
            }
        }

        // Required permissions via orchestrator (single boundary)
        refreshRequiredPermissionsDefinition()
    }

    fun refreshHealthConnectStatus() {
        refreshHealthConnectStatusSafe()
    }

    private suspend fun refreshGrantedPermissionsSafe() {
        refreshMutex.withLock {
            if (!canExposeProtectedUiData()) {
                grantedHealthPermissions.value = emptySet()
                return
            }

            when (val res = orchestrator.grantedHealthPermissionsSafe()) {
                is LifeFlowOrchestrator.ActionResult.Success -> {
                    grantedHealthPermissions.value =
                        if (canExposeProtectedUiData()) res.value else emptySet()
                }

                is LifeFlowOrchestrator.ActionResult.Error -> {
                    grantedHealthPermissions.value = emptySet()
                }

                is LifeFlowOrchestrator.ActionResult.Locked -> {
                    handleLockedProtectedOperation(res.reason)
                }
            }
        }
    }

    private suspend fun refreshWellbeingSnapshotSafe(identityInitialized: Boolean) {
        refreshMutex.withLock {
            if (!canExposeProtectedUiData()) {
                refreshPublicHealthStateOnly()
                return
            }

            when (val res = orchestrator.refreshWellbeingSnapshot(identityInitialized)) {
                is LifeFlowOrchestrator.ActionResult.Success -> {
                    applyWellbeingSnapshot(res.value)
                }

                is LifeFlowOrchestrator.ActionResult.Error -> {
                    refreshPublicHealthStateOnly()
                }

                is LifeFlowOrchestrator.ActionResult.Locked -> {
                    handleLockedProtectedOperation(res.reason)
                }
            }
        }
    }

    fun refreshGrantedPermissions() {
        viewModelScope.launch {
            runCatching {
                refreshGrantedPermissionsSafe()
            }.onFailure {
                handleUnexpectedProtectedRefreshFailure()
            }
        }
    }

    fun refreshMetricsAndTwinNow() {
        viewModelScope.launch {
            runCatching {
                refreshWellbeingSnapshotSafe(
                    identityInitialized = (uiState.value is UiState.Authenticated)
                )
            }.onFailure {
                handleUnexpectedProtectedRefreshFailure()
            }
        }
    }

    fun onHealthPermissionsResult(@Suppress("UNUSED_PARAMETER") granted: Set<String>) {
        viewModelScope.launch {
            runCatching {
                refreshWellbeingSnapshotSafe(
                    identityInitialized = (uiState.value is UiState.Authenticated)
                )
            }.onFailure {
                handleUnexpectedProtectedRefreshFailure()
            }
        }
    }

    fun onAuthenticationSuccess() {
        uiState.value = UiState.Loading

        viewModelScope.launch {
            if (!SecurityAccessSession.isAuthorized()) {
                failClosedWithError("Active auth session missing. Please authenticate again.")
                return@launch
            }

            when (SecurityRuleEngine.getTrustState()) {
                TrustState.COMPROMISED -> {
                    failClosedWithError(
                        "Security compromised. Reset vault is required before continuing."
                    )
                    return@launch
                }

                TrustState.DEGRADED -> {
                    failClosedWithError("Security degraded. Please authenticate again.")
                    return@launch
                }

                TrustState.VERIFIED -> {
                    // continue
                }
            }

            sessionExpiryNotified = false

            when (val boot = orchestrator.bootstrapIdentityIfNeeded()) {
                is LifeFlowOrchestrator.ActionResult.Success -> {
                    // Post-auth refresh is owned by MainActivity via
                    // LaunchedEffect(uiState is UiState.Authenticated)
                    uiState.value = UiState.Authenticated
                }

                is LifeFlowOrchestrator.ActionResult.Locked -> {
                    handleLockedProtectedOperation(boot.reason)
                    return@launch
                }

                is LifeFlowOrchestrator.ActionResult.Error -> {
                    failClosedWithError(boot.message)
                    return@launch
                }
            }
        }
    }

    fun onAuthenticationError(message: String) {
        failClosedWithError(message)
    }

    /**
     * Hard reset: keystore key + vault prefs + ciphertext blobs.
     * After reset we stay fail-closed and require a fresh auth.
     */
    fun resetVault() {
        uiState.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { performVaultReset() }
            SecurityAccessSession.clear()
            wipeUiCachesFailClosed()
            sessionExpiryNotified = false

            uiState.value = if (res.isSuccess) {
                UiState.Error("Vault reset complete. Please authenticate again.")
            } else {
                UiState.Error("Vault reset failed: ${res.exceptionOrNull()?.message ?: "unknown"}")
            }
        }
    }
}