package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun healthReadinessLabel(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        healthState != HealthConnectUiState.Available -> healthStateLabel(healthState)
        requiredCount == 0 -> "Waiting for health access"
        grantedCount == 0 -> "No health access yet"
        grantedCount < requiredCount -> "Partial access"
        else -> "Ready"
    }
}

internal fun permissionCoverageLabel(
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        requiredCount == 0 -> "Waiting"
        else -> "$grantedCount / $requiredCount"
    }
}

internal fun digitalTwinReadinessLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "Not loaded yet"
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK ->
            "Complete"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Access blocked"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            "Blocked"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "No data"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.UNKNOWN &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.UNKNOWN ->
            "Unknown"

        else ->
            "Partial"
    }
}

internal fun digitalTwinSignalCoverageLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "0 / 2"
    }

    var availableSignals = 0
    if (digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }
    if (digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    return "$availableSignals / 2"
}

internal fun dashboardNextMoveLabel(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Open Health Connect settings"

        digitalTwinState == null ->
            "Load first snapshot"

        requiredCount == 0 ->
            "Refresh after access loads"

        grantedCount < requiredCount ->
            "Review health access"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Review health access"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "Refresh later for new data"

        else ->
            "Dashboard ready"
    }
}

internal fun healthNextMoveLabel(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        healthState == HealthConnectUiState.NotInstalled ->
            "Install Health Connect"

        healthState == HealthConnectUiState.UpdateRequired ->
            "Update Health Connect"

        healthState == HealthConnectUiState.NotSupported ->
            "Unavailable on this device"

        healthState != HealthConnectUiState.Available ->
            "Open Health Connect settings"

        requiredCount == 0 ->
            "Wait for health access"

        grantedCount < requiredCount ->
            "Review health access"

        else ->
            "Health path ready"
    }
}

internal fun digitalTwinNextMoveLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "Load first snapshot"
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Review health access"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            "Re-authenticate"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "Refresh later"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.UNKNOWN &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.UNKNOWN ->
            "Refresh now"

        digitalTwinState.stepsAvailability != DigitalTwinState.Availability.OK ||
                digitalTwinState.heartRateAvailability != DigitalTwinState.Availability.OK ->
            "Refresh now"

        else ->
            "Snapshot ready"
    }
}
