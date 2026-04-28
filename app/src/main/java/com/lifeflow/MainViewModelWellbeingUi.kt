package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.WellbeingRefreshSnapshot
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal data class MainViewModelWellbeingUiUpdate(
    val healthConnectState: HealthConnectUiState? = null,
    val requiredHealthPermissions: Set<String>? = null,
    val grantedHealthPermissions: Set<String>? = null,
    val healthPermissionsInitError: String? = null,
    val digitalTwinState: DigitalTwinState? = null,
    val wellbeingAssessment: WellbeingAssessment? = null,
    val updateHealthPermissionsInitError: Boolean = true,
    val updateDigitalTwinState: Boolean = true,
    val updateWellbeingAssessment: Boolean = true,
    val lastAction: String
)

internal fun mainViewModelFailClosedUiUpdate(): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        grantedHealthPermissions = emptySet(),
        digitalTwinState = null,
        wellbeingAssessment = null,
        updateHealthPermissionsInitError = false,
        updateDigitalTwinState = true,
        updateWellbeingAssessment = true,
        lastAction = "Protected dashboard data cleared."
    )
}

internal fun mainViewModelPublicHealthStateOnlyUiUpdate(
    healthConnectState: HealthConnectUiState,
    requiredHealthPermissions: Set<String>,
    grantedHealthPermissions: Set<String>,
    healthPermissionsInitError: String?
): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        healthConnectState = healthConnectState,
        requiredHealthPermissions = requiredHealthPermissions,
        grantedHealthPermissions = grantedHealthPermissions,
        healthPermissionsInitError = healthPermissionsInitError,
        digitalTwinState = null,
        wellbeingAssessment = null,
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
        updateWellbeingAssessment = true,
        lastAction = "Public Health Connect state refreshed."
    )
}

internal fun mainViewModelProtectedSnapshotUiUpdate(
    snapshot: WellbeingRefreshSnapshot
): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        healthConnectState = snapshot.healthConnectState,
        requiredHealthPermissions = snapshot.requiredPermissions,
        grantedHealthPermissions = snapshot.grantedPermissions,
        healthPermissionsInitError = null,
        digitalTwinState = snapshot.digitalTwinState,
        wellbeingAssessment = snapshot.wellbeingAssessment,
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
        updateWellbeingAssessment = true,
        lastAction = buildString {
            append("Dashboard snapshot updated (")
            append(snapshot.grantedPermissions.size)
            append("/")
            append(snapshot.requiredPermissions.size)
            append(" permissions granted).")
        }
    )
}

internal fun mainViewModelProtectedSnapshotFailClosedUiUpdate(
    snapshot: WellbeingRefreshSnapshot
): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        healthConnectState = snapshot.healthConnectState,
        requiredHealthPermissions = snapshot.requiredPermissions,
        grantedHealthPermissions = emptySet(),
        healthPermissionsInitError = null,
        digitalTwinState = null,
        wellbeingAssessment = null,
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
        updateWellbeingAssessment = true,
        lastAction = "Protected snapshot received but UI remained fail-closed."
    )
}
