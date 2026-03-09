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
import kotlinx.coroutines.launch

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

    private fun wipeUiCachesFailClosed() {
        // keep requiredHealthPermissions (static), wipe granted + twin (dynamic)
        grantedHealthPermissions.value = emptySet()
        digitalTwinState.value = null
    }

    private fun canExposeProtectedUiData(): Boolean {
        return uiState.value is UiState.Authenticated &&
                SecurityAccessSession.isAuthorized() &&
                SecurityRuleEngine.getTrustState() == TrustState.VERIFIED
    }

    init {
        // B2: Fail-closed reaction to TrustState changes
        viewModelScope.launch {
            SecurityRuleEngine.trustState.collect { state ->
                when (state) {
                    TrustState.COMPROMISED -> {
                        SecurityAccessSession.clear()
                        wipeUiCachesFailClosed()
                        uiState.value = UiState.Error(
                            "Security compromised. Reset vault is required before continuing."
                        )
                    }

                    TrustState.DEGRADED -> {
                        // React only if user was already authenticated and got downgraded.
                        if (uiState.value is UiState.Authenticated) {
                            SecurityAccessSession.clear()
                            wipeUiCachesFailClosed()
                            uiState.value = UiState.Error("Security degraded. Please authenticate again.")
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
            while (true) {
                delay(SESSION_POLL_MS)

                if (uiState.value is UiState.Authenticated) {
                    val authorized = SecurityAccessSession.isAuthorized()
                    if (!authorized) {
                        if (!sessionExpiryNotified) {
                            sessionExpiryNotified = true
                            wipeUiCachesFailClosed()
                            uiState.value = UiState.Error("Session expired. Please authenticate again.")
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

    fun refreshHealthConnectStatus() {
        healthConnectState.value = orchestrator.healthConnectUiState()
    }

    private suspend fun refreshGrantedPermissionsSafe() {
        if (!canExposeProtectedUiData()) {
            grantedHealthPermissions.value = emptySet()
            return
        }

        when (val res = orchestrator.grantedHealthPermissionsSafe()) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                grantedHealthPermissions.value =
                    if (canExposeProtectedUiData()) res.value else emptySet()
            }

            is LifeFlowOrchestrator.ActionResult.Error -> grantedHealthPermissions.value = emptySet()
            is LifeFlowOrchestrator.ActionResult.Locked -> grantedHealthPermissions.value = emptySet()
        }
    }

    fun refreshGrantedPermissions() {
        viewModelScope.launch { refreshGrantedPermissionsSafe() }
    }

    fun refreshMetricsAndTwinNow() {
        viewModelScope.launch {
            runCatching { refreshHealthConnectStatus() }
            runCatching { refreshGrantedPermissionsSafe() }
            runCatching {
                refreshTwinBestEffort(
                    identityInitialized = (uiState.value is UiState.Authenticated)
                )
            }
        }
    }

    fun onHealthPermissionsResult(@Suppress("UNUSED_PARAMETER") granted: Set<String>) {
        viewModelScope.launch {
            refreshGrantedPermissionsSafe()
            refreshTwinBestEffort(identityInitialized = (uiState.value is UiState.Authenticated))
        }
    }

    fun onAuthenticationSuccess() {
        uiState.value = UiState.Loading

        viewModelScope.launch {
            if (!SecurityAccessSession.isAuthorized()) {
                wipeUiCachesFailClosed()
                uiState.value = UiState.Error("Active auth session missing. Please authenticate again.")
                return@launch
            }

            when (SecurityRuleEngine.getTrustState()) {
                TrustState.COMPROMISED -> {
                    SecurityAccessSession.clear()
                    wipeUiCachesFailClosed()
                    uiState.value = UiState.Error(
                        "Security compromised. Reset vault is required before continuing."
                    )
                    return@launch
                }

                TrustState.DEGRADED -> {
                    SecurityAccessSession.clear()
                    wipeUiCachesFailClosed()
                    uiState.value = UiState.Error("Security degraded. Please authenticate again.")
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
                    SecurityAccessSession.clear()
                    wipeUiCachesFailClosed()
                    uiState.value = UiState.Error(boot.reason)
                    return@launch
                }

                is LifeFlowOrchestrator.ActionResult.Error -> {
                    SecurityAccessSession.clear()
                    wipeUiCachesFailClosed()
                    uiState.value = UiState.Error(boot.message)
                    return@launch
                }
            }
        }
    }

    private suspend fun refreshTwinBestEffort(identityInitialized: Boolean) {
        if (!canExposeProtectedUiData()) {
            digitalTwinState.value = null
            return
        }

        when (val res = orchestrator.refreshTwinBestEffort(identityInitialized)) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                digitalTwinState.value =
                    if (canExposeProtectedUiData()) res.value else null
            }

            is LifeFlowOrchestrator.ActionResult.Error -> {
                digitalTwinState.value = null
            }

            is LifeFlowOrchestrator.ActionResult.Locked -> {
                wipeUiCachesFailClosed()
            }
        }
    }

    fun onAuthenticationError(message: String) {
        SecurityAccessSession.clear()
        wipeUiCachesFailClosed()
        uiState.value = UiState.Error(message)
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