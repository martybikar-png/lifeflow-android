package com.lifeflow.core

import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import kotlin.math.roundToLong
import kotlinx.coroutines.CancellationException

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
