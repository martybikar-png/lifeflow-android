package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.HolisticWellbeingNode
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase

internal class LifeFlowOrchestratorRefreshOperations(
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    private val getStepsLast24h: GetStepsLast24hUseCase,
    private val getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase,
    private val digitalTwinOrchestrator: DigitalTwinOrchestrator,
    private val wellbeingNode: HolisticWellbeingNode
) {
    fun healthConnectUiState(): HealthConnectUiState {
        return when (getHealthConnectStatus()) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
        }
    }

    suspend fun refreshTwinState(identityInitialized: Boolean): DigitalTwinState {
        return buildRefreshPipelineSnapshot(identityInitialized).digitalTwinState
    }

    suspend fun refreshWellbeingSnapshot(
        identityInitialized: Boolean
    ): WellbeingRefreshSnapshot {
        val refreshPipelineSnapshot = buildRefreshPipelineSnapshot(identityInitialized)
        val wellbeingAssessment = wellbeingNode.assess(refreshPipelineSnapshot.digitalTwinState)

        return WellbeingRefreshSnapshot(
            healthConnectState = refreshPipelineSnapshot.healthConnectState,
            requiredPermissions = refreshPipelineSnapshot.requiredPermissions,
            grantedPermissions = refreshPipelineSnapshot.grantedPermissions,
            stepsPermissionGranted = refreshPipelineSnapshot.permissionSnapshot.stepsPermissionGranted,
            heartRatePermissionGranted = refreshPipelineSnapshot.permissionSnapshot.heartRatePermissionGranted,
            digitalTwinState = refreshPipelineSnapshot.digitalTwinState,
            wellbeingAssessment = wellbeingAssessment
        )
    }

    private suspend fun buildRefreshPipelineSnapshot(
        identityInitialized: Boolean
    ): LifeFlowOrchestratorRefreshPipelineSnapshot {
        val healthConnectState = healthConnectUiState()
        val permissionBundle = lifeflowOrchestratorResolveRefreshPermissionBundleSafe(
            healthConnectState = healthConnectState,
            getHealthPermissions = getHealthPermissions,
            getGrantedHealthPermissions = getGrantedHealthPermissions
        )

        return lifeflowOrchestratorBuildRefreshPipelineSnapshot(
            identityInitialized = identityInitialized,
            healthConnectState = healthConnectState,
            permissionBundle = permissionBundle,
            getStepsLast24h = getStepsLast24h,
            getAvgHeartRateLast24h = getAvgHeartRateLast24h,
            digitalTwinOrchestrator = digitalTwinOrchestrator
        )
    }
}
