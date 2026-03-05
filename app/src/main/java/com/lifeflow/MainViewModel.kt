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

        // Static required permissions (safe to compute anytime)
        requiredHealthPermissions.value = try {
            getHealthPermissions()
        } catch (_: Throwable) {
            emptySet()
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

    fun onHealthPermissionsResult(granted: Set<String>) {
        // Health Connect returns the granted set after user action
        grantedHealthPermissions.value = granted

        // After granting, try to refresh metrics + twin (best-effort)
        viewModelScope.launch {
            // Only attempt metrics if already authenticated
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
                // If Activity/UI called this without an active auth session, we stop here.
                if (!SecurityAccessSession.isAuthorized()) {
                    uiState.value = UiState.Error(
                        "Active auth session missing. Please authenticate again."
                    )
                    return@launch
                }

                // (Optional but useful) Ensure we are not stuck in a degraded state after pre-auth denies.
                // This does NOT bypass security, because it only happens if session is already active.
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

                // Security gate passed
                uiState.value = UiState.Authenticated

            } catch (e: SecurityException) {
                // Security failures always clear the session (fail-closed)
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(e.message ?: "Security denied")
                return@launch
            } catch (t: Throwable) {
                // Bootstrap failure: also fail-closed
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(t.message ?: "Bootstrap failed")
                return@launch
            }

            // ✅ Non-security best-effort work AFTER successful identity bootstrap:
            // Do not break auth just because Health Connect is flaky.
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