package com.lifeflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun healthReadinessColor(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        healthState == HealthConnectUiState.NotInstalled ||
                healthState == HealthConnectUiState.NotSupported ||
                healthState == HealthConnectUiState.UpdateRequired ->
            colors.error

        healthState == HealthConnectUiState.Available &&
                requiredCount > 0 &&
                grantedCount >= requiredCount ->
            colors.primary

        healthState == HealthConnectUiState.Available &&
                requiredCount == 0 ->
            colors.tertiary

        healthState == HealthConnectUiState.Available &&
                grantedCount < requiredCount ->
            colors.tertiary

        else ->
            colors.onSurfaceVariant
    }
}

@Composable
internal fun permissionCoverageColor(
    requiredCount: Int,
    grantedCount: Int
): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        requiredCount == 0 -> colors.tertiary
        grantedCount >= requiredCount -> colors.primary
        else -> colors.tertiary
    }
}

@Composable
internal fun digitalTwinReadinessColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.onSurfaceVariant
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK ->
            colors.primary

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            colors.error

        else ->
            colors.tertiary
    }
}

@Composable
internal fun digitalTwinSignalCoverageColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.onSurfaceVariant
    }

    var availableSignals = 0
    if (digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }
    if (digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    return when (availableSignals) {
        2 -> colors.primary
        1 -> colors.tertiary
        else -> colors.onSurfaceVariant
    }
}

@Composable
internal fun dashboardNextMoveColor(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): Color {
    return when {
        healthState != HealthConnectUiState.Available ->
            MaterialTheme.colorScheme.error

        digitalTwinState == null ->
            MaterialTheme.colorScheme.tertiary

        requiredCount == 0 ->
            MaterialTheme.colorScheme.tertiary

        grantedCount < requiredCount ->
            MaterialTheme.colorScheme.tertiary

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            MaterialTheme.colorScheme.error

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            MaterialTheme.colorScheme.tertiary

        else ->
            MaterialTheme.colorScheme.primary
    }
}

@Composable
internal fun healthNextMoveColor(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Color {
    return when {
        healthState == HealthConnectUiState.NotInstalled ||
                healthState == HealthConnectUiState.NotSupported ||
                healthState == HealthConnectUiState.UpdateRequired ->
            MaterialTheme.colorScheme.error

        healthState != HealthConnectUiState.Available ->
            MaterialTheme.colorScheme.tertiary

        requiredCount == 0 ->
            MaterialTheme.colorScheme.tertiary

        grantedCount < requiredCount ->
            MaterialTheme.colorScheme.tertiary

        else ->
            MaterialTheme.colorScheme.primary
    }
}

@Composable
internal fun digitalTwinNextMoveColor(
    digitalTwinState: DigitalTwinState?
): Color {
    if (digitalTwinState == null) {
        return MaterialTheme.colorScheme.tertiary
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            MaterialTheme.colorScheme.error

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            MaterialTheme.colorScheme.error

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            MaterialTheme.colorScheme.tertiary

        digitalTwinState.stepsAvailability != DigitalTwinState.Availability.OK ||
                digitalTwinState.heartRateAvailability != DigitalTwinState.Availability.OK ->
            MaterialTheme.colorScheme.tertiary

        else ->
            MaterialTheme.colorScheme.primary
    }
}

@Composable
internal fun grantedStateColor(granted: Boolean): Color {
    return if (granted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

@Composable
internal fun yesNoColor(value: Boolean): Color {
    return if (value) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
internal fun availabilityColor(
    availability: DigitalTwinState.Availability
): Color {
    val colors = MaterialTheme.colorScheme

    return when (availability) {
        DigitalTwinState.Availability.OK -> colors.primary
        DigitalTwinState.Availability.PERMISSION_DENIED -> colors.error
        DigitalTwinState.Availability.BLOCKED -> colors.error
        DigitalTwinState.Availability.NO_DATA -> colors.tertiary
        DigitalTwinState.Availability.UNKNOWN -> colors.onSurfaceVariant
    }
}
