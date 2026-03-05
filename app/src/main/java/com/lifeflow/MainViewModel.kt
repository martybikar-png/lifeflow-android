package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToLong

class MainViewModel(
    private val repository: IdentityRepository,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    private val getStepsLast24h: GetStepsLast24hUseCase,
    private val getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase
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

    // Bound from Activity
    private var digitalTwinOrchestrator: DigitalTwinOrchestrator? = null

    init {
        // Fail-closed reaction to TrustState changes
        viewModelScope.launch {
            SecurityRuleEngine.trustState.collect { state ->
                if (state == TrustState.COMPROMISED) {
                    SecurityAccessSession.clear()
                    uiState.value = UiState.Error(
                        "Security compromised. You can re-authenticate or reset the vault."
                    )
                }
            }
        }

        // Static required permissions (safe to compute anytime) — NEVER silent failure
        try {
            requiredHealthPermissions.value = getHealthPermissions()
            healthPermissionsInitError.value = null
        } catch (t: Throwable) {
            requiredHealthPermissions.value = emptySet()
            healthPermissionsInitError.value =
                "${t::class.java.simpleName}: ${t.message ?: "unknown error"}"
        }
    }

    fun bindDigitalTwin(orchestrator: DigitalTwinOrchestrator) {
        digitalTwinOrchestrator = orchestrator
    }

    private fun refreshDigitalTwin(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?
    ) {
        val orch = digitalTwinOrchestrator ?: return

        digitalTwinState.value = orch.refresh(
            identityInitialized = identityInitialized,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h
        )
    }

    fun refreshHealthConnectStatus() {
        val status = getHealthConnectStatus()
        healthConnectState.value = when (status) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
            else -> HealthConnectUiState.Unknown
        }
    }

    private suspend fun refreshGrantedPermissionsSafe() {
        grantedHealthPermissions.value = try {
            getGrantedHealthPermissions()
        } catch (_: Throwable) {
            emptySet()
        }
    }

    fun refreshGrantedPermissions() {
        viewModelScope.launch { refreshGrantedPermissionsSafe() }
    }

    fun onHealthPermissionsResult(@Suppress("UNUSED_PARAMETER") granted: Set<String>) {
        // Do NOT trust callback payload as the only source of truth (OEM/HC can be flaky)
        viewModelScope.launch {
            refreshGrantedPermissionsSafe()

            // After granting, try to refresh metrics + twin (best-effort)
            if (uiState.value is UiState.Authenticated) {
                refreshMetricsAndTwinBestEffort()
            }
        }
    }

    fun onAuthenticationSuccess() {
        uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                // ✅ HARD FAIL-CLOSED GATE:
                if (!SecurityAccessSession.isAuthorized()) {
                    uiState.value = UiState.Error(
                        "Active auth session missing. Please authenticate again."
                    )
                    return@launch
                }

                // This does NOT bypass security (only runs if session already active)
                SecurityRuleEngine.setTrustState(
                    TrustState.VERIFIED,
                    reason = "Auth session active (post-biometric)"
                )

                val active = repository.getActiveIdentity()

                if (active == null) {
                    val newIdentity = LifeFlowIdentity(
                        id = UUID.randomUUID(),
                        createdAtEpochMillis = System.currentTimeMillis(),
                        isActive = true
                    )
                    repository.save(newIdentity)
                }

                uiState.value = UiState.Authenticated

            } catch (e: SecurityException) {
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(e.message ?: "Security denied")
                return@launch
            } catch (t: Throwable) {
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(t.message ?: "Bootstrap failed")
                return@launch
            }

            // ✅ Non-security best-effort AFTER successful identity bootstrap
            try { refreshHealthConnectStatus() } catch (_: Throwable) {}
            try { refreshGrantedPermissionsSafe() } catch (_: Throwable) {}
            try { refreshMetricsAndTwinBestEffort() } catch (_: Throwable) {}
        }
    }

    private suspend fun refreshMetricsAndTwinBestEffort() {
        val (steps, avgHr) = if (healthConnectState.value is HealthConnectUiState.Available) {
            try {
                val s = getStepsLast24h()
                val hr = getAvgHeartRateLast24h()?.roundToLong()
                Pair(s, hr)
            } catch (_: Throwable) {
                Pair(null, null)
            }
        } else {
            Pair(null, null)
        }

        refreshDigitalTwin(
            identityInitialized = true,
            stepsLast24h = steps,
            avgHeartRateLast24h = avgHr
        )
    }

    fun onAuthenticationError(message: String) {
        SecurityAccessSession.clear()
        uiState.value = UiState.Error(message)
    }
}

sealed class HealthConnectUiState {
    object Unknown : HealthConnectUiState()
    object Available : HealthConnectUiState()
    object NotInstalled : HealthConnectUiState()
    object NotSupported : HealthConnectUiState()
    object UpdateRequired : HealthConnectUiState()
}