package com.lifeflow.core

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.HolisticWellbeingNode
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase

class LifeFlowOrchestratorProtectedOperations(
    private val identityRepository: IdentityRepository,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    getHealthConnectStatus: GetHealthConnectStatusUseCase,
    getStepsLast24h: GetStepsLast24hUseCase,
    getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase,
    digitalTwinOrchestrator: DigitalTwinOrchestrator,
    wellbeingNode: HolisticWellbeingNode,
    private val tierManager: TierManager
) {
    private val coreOperationAccess = LifeFlowOrchestratorCoreOperationAccess(
        tierManager = tierManager
    )

    private val refreshOperations = LifeFlowOrchestratorRefreshOperations(
        getHealthConnectStatus = getHealthConnectStatus,
        getHealthPermissions = getHealthPermissions,
        getGrantedHealthPermissions = getGrantedHealthPermissions,
        getStepsLast24h = getStepsLast24h,
        getAvgHeartRateLast24h = getAvgHeartRateLast24h,
        digitalTwinOrchestrator = digitalTwinOrchestrator,
        wellbeingNode = wellbeingNode
    )

    fun currentTier(): TierState = tierManager.currentTier()

    fun healthConnectUiState(): HealthConnectUiState =
        refreshOperations.healthConnectUiState()

    fun requiredHealthPermissionsSafe(): ActionResult<Set<String>> =
        lifeflowOrchestratorLookupRequiredHealthPermissions(getHealthPermissions)

    suspend fun grantedHealthPermissionsSafe(): ActionResult<Set<String>> =
        lifeflowOrchestratorLookupGrantedHealthPermissions(getGrantedHealthPermissions)

    suspend fun bootstrapIdentityIfNeeded(): ActionResult<Unit> {
        return coreOperationAccess.run(
            operationPolicy = LifeFlowOrchestratorPolicies.bootstrapIdentity
        ) {
            lifeflowOrchestratorBootstrapIdentityIfNeeded(identityRepository)
        }
    }

    suspend fun refreshTwinBestEffort(identityInitialized: Boolean): ActionResult<DigitalTwinState> {
        return coreOperationAccess.runValue(
            operationPolicy = LifeFlowOrchestratorPolicies.refreshTwin,
            accessPolicy = LifeFlowOrchestratorPolicies.refreshTwinAccess
        ) {
            refreshOperations.refreshTwinState(identityInitialized)
        }
    }

    suspend fun refreshWellbeingSnapshot(identityInitialized: Boolean): ActionResult<WellbeingRefreshSnapshot> {
        return coreOperationAccess.runValue(
            operationPolicy = LifeFlowOrchestratorPolicies.refreshWellbeing,
            accessPolicy = LifeFlowOrchestratorPolicies.refreshWellbeingAccess
        ) {
            refreshOperations.refreshWellbeingSnapshot(identityInitialized)
        }
    }
}

