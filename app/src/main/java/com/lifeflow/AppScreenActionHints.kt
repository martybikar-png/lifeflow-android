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
            "Protected session is already active."
        healthState == HealthConnectUiState.NotInstalled ->
            "Open settings and finish Health Connect setup."
        healthState == HealthConnectUiState.UpdateRequired ->
            "Update Health Connect in settings first."
        canGrantHealthPermissions(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ) ->
            "Authenticate now, or review Health access."
        else ->
            "Everything is ready for the first dashboard unlock."
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

