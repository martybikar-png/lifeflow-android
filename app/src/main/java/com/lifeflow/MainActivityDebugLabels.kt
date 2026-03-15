package com.lifeflow

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun uiStateDebugLabel(uiState: UiState): String {
    return when (uiState) {
        UiState.Loading -> "Loading"
        UiState.Authenticated -> "Authenticated"
        is UiState.Error -> "Error"
    }
}

internal fun permissionContractLabel(requiredCount: Int): String {
    return if (requiredCount == 0) {
        "Pending"
    } else {
        "$requiredCount required"
    }
}

internal fun permissionReadinessLabel(
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        requiredCount == 0 -> "No required contract resolved"
        grantedCount == 0 -> "0 / $requiredCount granted"
        grantedCount < requiredCount -> "$grantedCount / $requiredCount granted"
        else -> "Ready ($grantedCount / $requiredCount)"
    }
}

internal fun metricPermissionLabel(granted: Boolean): String {
    return if (granted) "Granted" else "Not granted"
}

internal fun digitalTwinDebugLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "Not loaded yet"
    }

    return when {
        hasFullCardSignalCoverage(digitalTwinState) ->
            "Complete"

        hasPermissionDeniedCardSignal(digitalTwinState) ->
            "Permission blocked"

        hasNoCardSignalData(digitalTwinState) ->
            "No data"

        hasBlockedCardSignal(digitalTwinState) ->
            "Blocked"

        else ->
            "Partial"
    }
}