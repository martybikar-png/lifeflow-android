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
        isHealthColorUnavailable(healthState) ->
            colors.error

        isHealthColorReady(healthState, requiredCount, grantedCount) ->
            colors.primary

        healthState == HealthConnectUiState.Available && requiredCount == 0 ->
            colors.tertiary

        healthState == HealthConnectUiState.Available && grantedCount < requiredCount ->
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
internal fun healthNextMoveColor(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        isHealthColorUnavailable(healthState) ->
            colors.error

        healthState != HealthConnectUiState.Available ->
            colors.tertiary

        requiredCount == 0 ->
            colors.tertiary

        grantedCount < requiredCount ->
            colors.tertiary

        else ->
            colors.primary
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

private fun isHealthColorUnavailable(healthState: HealthConnectUiState): Boolean {
    return healthState == HealthConnectUiState.NotInstalled ||
            healthState == HealthConnectUiState.NotSupported ||
            healthState == HealthConnectUiState.UpdateRequired
}

private fun isHealthColorReady(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Boolean {
    return healthState == HealthConnectUiState.Available &&
            requiredCount > 0 &&
            grantedCount >= requiredCount
}