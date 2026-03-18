package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.WellbeingRefreshSnapshot
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal data class MainViewModelWellbeingUiUpdate(
    val healthConnectState: HealthConnectUiState? = null,
    val requiredHealthPermissions: Set<String>? = null,
    val grantedHealthPermissions: Set<String>? = null,
    val healthPermissionsInitError: String? = null,
    val digitalTwinState: DigitalTwinState? = null,
    val updateHealthPermissionsInitError: Boolean = true,
    val updateDigitalTwinState: Boolean = true,
    val lastAction: String
)

internal fun mainViewModelFailClosedUiUpdate(): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        grantedHealthPermissions = emptySet(),
        digitalTwinState = null,
        updateHealthPermissionsInitError = false,
        updateDigitalTwinState = true,
        lastAction = "Protected dashboard data cleared."
    )
}

internal fun mainViewModelPublicHealthStateOnlyUiUpdate(
    healthConnectState: HealthConnectUiState,
    requiredHealthPermissions: Set<String>,
    healthPermissionsInitError: String?
): MainViewModelWellbeingUiUpdate {
    return MainViewModelWellbeingUiUpdate(
        healthConnectState = healthConnectState,
        requiredHealthPermissions = requiredHealthPermissions,
        grantedHealthPermissions = emptySet(),
        healthPermissionsInitError = healthPermissionsInitError,
        digitalTwinState = null,
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
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
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
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
        updateHealthPermissionsInitError = true,
        updateDigitalTwinState = true,
        lastAction = "Protected snapshot received but UI remained fail-closed."
    )
}