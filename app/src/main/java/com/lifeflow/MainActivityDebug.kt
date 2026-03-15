package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun buildDebugLines(
    uiState: UiState,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    digitalTwinState: DigitalTwinState?
): List<String> {
    return buildList {
        add("UI: ${uiStateDebugLabel(uiState)}")
        add("Health state: ${healthStateLabel(healthState)}")
        add("Permission contract: ${permissionContractLabel(requiredCount)}")
        add("Permissions: ${permissionReadinessLabel(requiredCount, grantedCount)}")
        add("Steps permission: ${metricPermissionLabel(stepsGranted)}")
        add("Heart rate permission: ${metricPermissionLabel(hrGranted)}")
        add("Twin status: ${digitalTwinDebugLabel(digitalTwinState)}")

        if (digitalTwinState != null) {
            addAll(buildDigitalTwinDetailLines(digitalTwinState))
        }
    }
}

private fun buildDigitalTwinDetailLines(
    digitalTwinState: DigitalTwinState
): List<String> {
    return listOf(
        "Twin identity initialized: ${yesNoLabel(digitalTwinState.identityInitialized)}",
        "Twin steps availability: ${availabilityLabel(digitalTwinState.stepsAvailability)}",
        "Twin heart rate availability: ${availabilityLabel(digitalTwinState.heartRateAvailability)}",
        "Twin steps value: ${digitalTwinState.stepsLast24h?.toString() ?: "—"}",
        "Twin avg heart rate: ${digitalTwinState.avgHeartRateLast24h?.toString() ?: "—"}",
        "Twin last updated: ${formatLastUpdated(digitalTwinState.lastUpdatedEpochMillis)}",
        "Twin notes: ${digitalTwinState.notes.size}"
    )
}