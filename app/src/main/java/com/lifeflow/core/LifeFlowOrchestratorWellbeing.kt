package com.lifeflow.core

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import kotlin.math.roundToLong
import kotlinx.coroutines.CancellationException

internal data class LifeFlowOrchestratorRefreshPermissionBundle(
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val permissionSnapshot: MetricPermissionSnapshot
)

internal data class LifeFlowOrchestratorRefreshPipelineSnapshot(
    val healthConnectState: HealthConnectUiState,
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val permissionSnapshot: MetricPermissionSnapshot,
    val metricReadSnapshot: MetricReadSnapshot,
    val digitalTwinState: DigitalTwinState
)

internal suspend fun lifeflowOrchestratorResolveRefreshPermissionBundleSafe(
    healthConnectState: HealthConnectUiState,
    getHealthPermissions: GetHealthPermissionsUseCase,
    getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase
): LifeFlowOrchestratorRefreshPermissionBundle {
    val requiredPermissions = when (
        val result = lifeflowOrchestratorLookupRequiredHealthPermissions(getHealthPermissions)
    ) {
        is ActionResult.Success -> result.value
        is ActionResult.Locked -> return lifeflowOrchestratorEmptyRefreshPermissionBundle()
        is ActionResult.Error -> return lifeflowOrchestratorEmptyRefreshPermissionBundle()
    }

    if (requiredPermissions.isEmpty()) {
        return lifeflowOrchestratorEmptyRefreshPermissionBundle()
    }

    val grantedPermissions = when (
        val result = lifeflowOrchestratorLookupGrantedHealthPermissions(getGrantedHealthPermissions)
    ) {
        is ActionResult.Success -> result.value
        is ActionResult.Locked -> emptySet()
        is ActionResult.Error -> emptySet()
    }

    return lifeflowOrchestratorResolveRefreshPermissionBundle(
        healthConnectState = healthConnectState,
        requiredPermissions = requiredPermissions,
        grantedPermissions = grantedPermissions
    )
}

internal fun lifeflowOrchestratorResolveRefreshPermissionBundle(
    healthConnectState: HealthConnectUiState,
    requiredPermissions: Set<String>,
    grantedPermissions: Set<String>
): LifeFlowOrchestratorRefreshPermissionBundle {
    if (requiredPermissions.isEmpty()) {
        return lifeflowOrchestratorEmptyRefreshPermissionBundle()
    }

    val permissionSnapshot = if (healthConnectState is HealthConnectUiState.Available) {
        lifeflowOrchestratorResolveMetricPermissionSnapshot(
            requiredPermissions = requiredPermissions,
            grantedPermissions = grantedPermissions
        )
    } else {
        lifeflowOrchestratorEmptyMetricPermissionSnapshot()
    }

    return LifeFlowOrchestratorRefreshPermissionBundle(
        requiredPermissions = requiredPermissions,
        grantedPermissions = grantedPermissions,
        permissionSnapshot = permissionSnapshot
    )
}

internal fun lifeflowOrchestratorEmptyRefreshPermissionBundle(): LifeFlowOrchestratorRefreshPermissionBundle {
    return LifeFlowOrchestratorRefreshPermissionBundle(
        requiredPermissions = emptySet(),
        grantedPermissions = emptySet(),
        permissionSnapshot = lifeflowOrchestratorEmptyMetricPermissionSnapshot()
    )
}

internal suspend fun lifeflowOrchestratorBuildRefreshPipelineSnapshot(
    identityInitialized: Boolean,
    healthConnectState: HealthConnectUiState,
    permissionBundle: LifeFlowOrchestratorRefreshPermissionBundle,
    getStepsLast24h: GetStepsLast24hUseCase,
    getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase,
    digitalTwinOrchestrator: DigitalTwinOrchestrator
): LifeFlowOrchestratorRefreshPipelineSnapshot {
    val metricReadSnapshot = lifeflowOrchestratorReadMetricsBestEffort(
        healthConnectState = healthConnectState,
        permissionSnapshot = permissionBundle.permissionSnapshot,
        getStepsLast24h = getStepsLast24h,
        getAvgHeartRateLast24h = getAvgHeartRateLast24h
    )

    val digitalTwinState = lifeflowOrchestratorRefreshDigitalTwin(
        identityInitialized = identityInitialized,
        metricReadSnapshot = metricReadSnapshot,
        permissionSnapshot = permissionBundle.permissionSnapshot,
        digitalTwinOrchestrator = digitalTwinOrchestrator
    )

    return LifeFlowOrchestratorRefreshPipelineSnapshot(
        healthConnectState = healthConnectState,
        requiredPermissions = permissionBundle.requiredPermissions,
        grantedPermissions = permissionBundle.grantedPermissions,
        permissionSnapshot = permissionBundle.permissionSnapshot,
        metricReadSnapshot = metricReadSnapshot,
        digitalTwinState = digitalTwinState
    )
}

/**
 * Resolve permission status per metric using already-known permission sets.
 *
 * Semantics:
 * - true  -> permission resolved and granted
 * - false -> permission resolved and not granted
 * - null  -> permission state unknown / not required / not resolvable right now
 */
internal fun lifeflowOrchestratorResolveMetricPermissionSnapshot(
    requiredPermissions: Set<String>,
    grantedPermissions: Set<String>
): MetricPermissionSnapshot {
    if (requiredPermissions.isEmpty()) {
        return lifeflowOrchestratorEmptyMetricPermissionSnapshot()
    }

    val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
    val heartRatePermission = HealthPermission.getReadPermission(HeartRateRecord::class)

    val stepsRequired = requiredPermissions.contains(stepsPermission)
    val heartRateRequired = requiredPermissions.contains(heartRatePermission)

    val stepsGranted = if (stepsRequired) {
        grantedPermissions.contains(stepsPermission)
    } else {
        null
    }

    val heartRateGranted = if (heartRateRequired) {
        grantedPermissions.contains(heartRatePermission)
    } else {
        null
    }

    return MetricPermissionSnapshot(
        stepsPermissionGranted = stepsGranted,
        heartRatePermissionGranted = heartRateGranted
    )
}

internal fun lifeflowOrchestratorEmptyMetricPermissionSnapshot(): MetricPermissionSnapshot {
    return MetricPermissionSnapshot(
        stepsPermissionGranted = null,
        heartRatePermissionGranted = null
    )
}

/**
 * Best-effort metric read behind consent boundary.
 * No throw, deterministic null fallback.
 */
internal suspend fun lifeflowOrchestratorReadMetricsBestEffort(
    healthConnectState: HealthConnectUiState,
    permissionSnapshot: MetricPermissionSnapshot,
    getStepsLast24h: GetStepsLast24hUseCase,
    getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase
): MetricReadSnapshot {
    if (healthConnectState !is HealthConnectUiState.Available) {
        return MetricReadSnapshot(
            stepsLast24h = null,
            avgHeartRateLast24h = null
        )
    }

    var steps: Long? = null
    var heartRate: Long? = null

    if (permissionSnapshot.stepsPermissionGranted == true) {
        try {
            steps = getStepsLast24h()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // keep null; engine will classify deterministically
        }
    }

    if (permissionSnapshot.heartRatePermissionGranted == true) {
        try {
            heartRate = getAvgHeartRateLast24h()?.roundToLong()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // keep null; engine will classify deterministically
        }
    }

    return MetricReadSnapshot(
        stepsLast24h = steps,
        avgHeartRateLast24h = heartRate
    )
}

internal fun lifeflowOrchestratorRefreshDigitalTwin(
    identityInitialized: Boolean,
    metricReadSnapshot: MetricReadSnapshot,
    permissionSnapshot: MetricPermissionSnapshot,
    digitalTwinOrchestrator: DigitalTwinOrchestrator
): DigitalTwinState {
    return digitalTwinOrchestrator.refresh(
        identityInitialized = identityInitialized,
        stepsLast24h = metricReadSnapshot.stepsLast24h,
        avgHeartRateLast24h = metricReadSnapshot.avgHeartRateLast24h,
        stepsPermissionGranted = permissionSnapshot.stepsPermissionGranted,
        heartRatePermissionGranted = permissionSnapshot.heartRatePermissionGranted
    )
}
