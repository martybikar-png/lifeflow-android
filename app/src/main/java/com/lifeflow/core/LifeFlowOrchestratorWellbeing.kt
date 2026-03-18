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

internal suspend fun lifeflowOrchestratorResolveMetricPermissionSnapshotSafe(
    getHealthPermissions: GetHealthPermissionsUseCase,
    getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase
): MetricPermissionSnapshot {
    return try {
        val requiredPermissions = getHealthPermissions()
        if (requiredPermissions.isEmpty()) {
            return lifeflowOrchestratorEmptyMetricPermissionSnapshot()
        }

        val grantedPermissions = getGrantedHealthPermissions()
        lifeflowOrchestratorResolveMetricPermissionSnapshot(
            requiredPermissions = requiredPermissions,
            grantedPermissions = grantedPermissions
        )
    } catch (_: Throwable) {
        lifeflowOrchestratorEmptyMetricPermissionSnapshot()
    }
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
        } catch (_: Throwable) {
            // keep null; engine will classify deterministically
        }
    }

    if (permissionSnapshot.heartRatePermissionGranted == true) {
        try {
            heartRate = getAvgHeartRateLast24h()?.roundToLong()
        } catch (_: Throwable) {
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