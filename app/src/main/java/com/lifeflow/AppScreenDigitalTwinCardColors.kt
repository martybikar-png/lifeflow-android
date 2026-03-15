package com.lifeflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun digitalTwinReadinessColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.onSurfaceVariant
    }

    return when {
        hasFullCardSignalCoverage(digitalTwinState) ->
            colors.primary

        hasPermissionDeniedCardSignal(digitalTwinState) || hasBlockedCardSignal(digitalTwinState) ->
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

    return when (countAvailableCardSignals(digitalTwinState)) {
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
    val colors = MaterialTheme.colorScheme

    return when {
        healthState != HealthConnectUiState.Available ->
            colors.error

        digitalTwinState == null ->
            colors.tertiary

        requiredCount == 0 ->
            colors.tertiary

        grantedCount < requiredCount ->
            colors.tertiary

        hasPermissionDeniedCardSignal(digitalTwinState) ->
            colors.error

        hasNoCardSignalData(digitalTwinState) ->
            colors.tertiary

        else ->
            colors.primary
    }
}

@Composable
internal fun digitalTwinNextMoveColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.tertiary
    }

    return when {
        hasPermissionDeniedCardSignal(digitalTwinState) ->
            colors.error

        hasBlockedCardSignal(digitalTwinState) ->
            colors.error

        hasNoCardSignalData(digitalTwinState) ->
            colors.tertiary

        !hasFullCardSignalCoverage(digitalTwinState) ->
            colors.tertiary

        else ->
            colors.primary
    }
}