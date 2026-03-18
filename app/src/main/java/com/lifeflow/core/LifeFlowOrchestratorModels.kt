package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal data class MetricPermissionSnapshot(
    val stepsPermissionGranted: Boolean?,
    val heartRatePermissionGranted: Boolean?
)

internal data class MetricReadSnapshot(
    val stepsLast24h: Long?,
    val avgHeartRateLast24h: Long?
)

sealed class ActionResult<out T> {
    data class Success<T>(val value: T) : ActionResult<T>()
    data class Locked(val reason: String) : ActionResult<Nothing>()
    data class Error(val message: String) : ActionResult<Nothing>()
}

data class WellbeingRefreshSnapshot(
    val healthConnectState: HealthConnectUiState,
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val stepsPermissionGranted: Boolean?,
    val heartRatePermissionGranted: Boolean?,
    val digitalTwinState: DigitalTwinState,
    val wellbeingAssessment: WellbeingAssessment
)
