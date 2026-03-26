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
        add("Flow branch: ${uiFlowBranchLabel(uiState)}")
        add("Health state: ${healthStateLabel(healthState)}")
        add("Permission contract: ${permissionContractLabel(requiredCount)}")
        add("Permissions: ${permissionReadinessLabel(requiredCount, grantedCount)}")
        add("Steps permission: ${metricPermissionLabel(stepsGranted)}")
        add("Heart rate permission: ${metricPermissionLabel(hrGranted)}")
        add("Twin status: ${digitalTwinDebugLabel(digitalTwinState)}")
        add("Flow note: ${uiFlowNote(uiState, digitalTwinState)}")

        if (digitalTwinState != null) {
            addAll(buildDigitalTwinDetailLines(digitalTwinState))
        }
    }
}

private fun uiFlowBranchLabel(uiState: UiState): String {
    return when (uiState) {
        UiState.Loading -> "Startup or auth preparation"
        UiState.Authenticated -> "Core shell navigation"
        is UiState.FreeTier -> "Free shell navigation"
        is UiState.Error -> "Recovery / attention state"
    }
}

private fun uiFlowNote(
    uiState: UiState,
    digitalTwinState: DigitalTwinState?
): String {
    return when (uiState) {
        UiState.Loading ->
            "Waiting for the next safe UI branch."
        UiState.Authenticated ->
            if (digitalTwinState == null) {
                "Authenticated shell is active. Twin data is not loaded yet."
            } else {
                "Authenticated shell is active with protected twin visibility."
            }
        is UiState.FreeTier ->
            "Free shell is active. Core-only protected flows stay inactive."
        is UiState.Error ->
            "Recovery branch is active until the next safe action succeeds."
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
