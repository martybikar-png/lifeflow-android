package com.lifeflow.core

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase

/**
 * Phase A — Orchestration / Authority Expansion
 *
 * Single entrypoint for sensitive operations:
 * - identity bootstrap
 * - consent + HC status
 * - wellbeing reads
 * - digital twin refresh
 *
 * Zero-bypass principle: UI/ViewModel should call only this orchestrator
 * for sensitive work. Fail-closed for security gating.
 */
class LifeFlowOrchestrator(
    private val identityRepository: IdentityRepository,
    private val digitalTwinOrchestrator: DigitalTwinOrchestrator,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    private val getStepsLast24h: GetStepsLast24hUseCase,
    private val getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase
) {

    fun healthConnectUiState(): HealthConnectUiState {
        return when (getHealthConnectStatus()) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
        }
    }

    fun requiredHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getHealthPermissions())
        } catch (t: Throwable) {
            ActionResult.Error("${t::class.java.simpleName}: ${t.message ?: "unknown error"}")
        }

    suspend fun grantedHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getGrantedHealthPermissions())
        } catch (_: Throwable) {
            ActionResult.Success(emptySet())
        }

    suspend fun bootstrapIdentityIfNeeded(): ActionResult<Unit> {
        return lifeflowOrchestratorBootstrapIdentityIfNeeded(identityRepository)
    }

    /**
     * Best-effort metrics read is NOT security-sensitive by itself,
     * but it MUST still obey consent boundary (HC availability + permissions).
     *
     * identityInitialized tells the twin whether identity core is ready.
     */
    suspend fun refreshTwinBestEffort(
        identityInitialized: Boolean
    ): ActionResult<DigitalTwinState> {
        val healthConnectState = healthConnectUiState()
        val permissionSnapshot = if (healthConnectState is HealthConnectUiState.Available) {
            lifeflowOrchestratorResolveMetricPermissionSnapshotSafe(
                getHealthPermissions = getHealthPermissions,
                getGrantedHealthPermissions = getGrantedHealthPermissions
            )
        } else {
            lifeflowOrchestratorEmptyMetricPermissionSnapshot()
        }

        val metricReadSnapshot = lifeflowOrchestratorReadMetricsBestEffort(
            healthConnectState = healthConnectState,
            permissionSnapshot = permissionSnapshot,
            getStepsLast24h = getStepsLast24h,
            getAvgHeartRateLast24h = getAvgHeartRateLast24h
        )

        val state = lifeflowOrchestratorRefreshDigitalTwin(
            identityInitialized = identityInitialized,
            metricReadSnapshot = metricReadSnapshot,
            permissionSnapshot = permissionSnapshot,
            digitalTwinOrchestrator = digitalTwinOrchestrator
        )

        return ActionResult.Success(state)
    }

    /**
     * Unified orchestration entrypoint for the next phase.
     *
     * Returns:
     * - current Health Connect state
     * - required/granted permission sets
     * - per-metric permission resolution
     * - freshly computed Digital Twin state
     *
     * This keeps the ViewModel slimmer and centralizes consent-aware
     * wellbeing refresh logic inside the orchestrator layer.
     */
    suspend fun refreshWellbeingSnapshot(
        identityInitialized: Boolean
    ): ActionResult<WellbeingRefreshSnapshot> {
        val healthConnectState = healthConnectUiState()

        val requiredPermissions = when (val result = requiredHealthPermissionsSafe()) {
            is ActionResult.Success -> result.value
            is ActionResult.Locked -> return result
            is ActionResult.Error -> emptySet()
        }

        val grantedPermissions = when (val result = grantedHealthPermissionsSafe()) {
            is ActionResult.Success -> result.value
            is ActionResult.Locked -> return result
            is ActionResult.Error -> emptySet()
        }

        val permissionSnapshot = if (healthConnectState is HealthConnectUiState.Available) {
            lifeflowOrchestratorResolveMetricPermissionSnapshot(
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions
            )
        } else {
            lifeflowOrchestratorEmptyMetricPermissionSnapshot()
        }

        val metricReadSnapshot = lifeflowOrchestratorReadMetricsBestEffort(
            healthConnectState = healthConnectState,
            permissionSnapshot = permissionSnapshot,
            getStepsLast24h = getStepsLast24h,
            getAvgHeartRateLast24h = getAvgHeartRateLast24h
        )

        val digitalTwinState = lifeflowOrchestratorRefreshDigitalTwin(
            identityInitialized = identityInitialized,
            metricReadSnapshot = metricReadSnapshot,
            permissionSnapshot = permissionSnapshot,
            digitalTwinOrchestrator = digitalTwinOrchestrator
        )

        return ActionResult.Success(
            WellbeingRefreshSnapshot(
                healthConnectState = healthConnectState,
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions,
                stepsPermissionGranted = permissionSnapshot.stepsPermissionGranted,
                heartRatePermissionGranted = permissionSnapshot.heartRatePermissionGranted,
                digitalTwinState = digitalTwinState
            )
        )
    }
}