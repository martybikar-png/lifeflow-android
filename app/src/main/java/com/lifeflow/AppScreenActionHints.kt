package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun loadingActionHint(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        isAuthenticating ->
            "Authentication is already running. Wait for the current protected check to complete."
        healthState == HealthConnectUiState.NotInstalled ->
            "Open settings first and finish the Health Connect installation path."
        healthState == HealthConnectUiState.UpdateRequired ->
            "Open settings first and update Health Connect before continuing."
        canGrantHealthPermissions(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ) ->
            "Authenticate now or review Health access to improve the first dashboard snapshot."
        else ->
            "Everything is lined up for the first protected dashboard unlock."
    }
}

internal fun dashboardActionHint(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Health Connect must be ready before the dashboard can refresh protected wellbeing data."
        digitalTwinState == null ->
            "Load the first Digital Twin snapshot to populate the dashboard."
        hasMissingHealthPermissions(requiredCount, grantedCount) ->
            "You can refresh now, but reviewing Health access will improve signal coverage."
        else ->
            "The dashboard is live. Refresh whenever you want a newer snapshot."
    }
}
