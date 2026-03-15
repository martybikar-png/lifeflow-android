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
        hasFullCardSignalCoverage(digitalTwinState) ->
            "Complete"

        hasPermissionDeniedCardSignal(digitalTwinState) ->
            "Access blocked"

        hasBlockedCardSignal(digitalTwinState) ->
            "Blocked"

        hasNoCardSignalData(digitalTwinState) ->
            "No data"

        hasUnknownCardSignalCoverage(digitalTwinState) ->
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

    return "${countAvailableCardSignals(digitalTwinState)} / 2"
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

        hasPermissionDeniedCardSignal(digitalTwinState) ->
            "Review health access"

        hasNoCardSignalData(digitalTwinState) ->
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
        hasPermissionDeniedCardSignal(digitalTwinState) ->
            "Review health access"

        hasBlockedCardSignal(digitalTwinState) ->
            "Re-authenticate"

        hasNoCardSignalData(digitalTwinState) ->
            "Refresh later"

        hasUnknownCardSignalCoverage(digitalTwinState) ->
            "Refresh now"

        !hasFullCardSignalCoverage(digitalTwinState) ->
            "Refresh now"

        else ->
            "Snapshot ready"
    }
}