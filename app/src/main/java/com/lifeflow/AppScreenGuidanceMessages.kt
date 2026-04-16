package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun loadingGuidanceMessage(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        isAuthenticating ->
            "Protected session is active. Waiting for checks to finish."

        healthState == HealthConnectUiState.NotInstalled ->
            "Install Health Connect, then come back and authenticate."

        healthState == HealthConnectUiState.UpdateRequired ->
            "Update Health Connect, then continue."

        canGrantHealthPermissions(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ) ->
            "Authenticate now, or review Health access for a fuller snapshot."

        else ->
            "Authenticate to open the first dashboard snapshot."
    }
}

internal fun dashboardGuidanceMessage(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Open Health Connect settings first. Until Health Connect is ready, the dashboard cannot expose full wellbeing data."

        digitalTwinState == null ->
            "Tap Refresh now to load the first Digital Twin snapshot."

        requiredCount == 0 ->
            "Health access is still loading. Refresh again after Health Connect resolves the available access set."

        guidanceHasMissingHealthPermissions(requiredCount, grantedCount) ->
            "Review Health access to improve snapshot coverage and unblock missing signals."

        guidanceHasPermissionDeniedSignal(digitalTwinState) ->
            "Some signals are blocked by Health access. Open Health Connect settings and review access to complete the snapshot."

        guidanceHasNoSignalData(digitalTwinState) ->
            "The dashboard is working, but there is not enough usable wellbeing data yet. Refresh again later after more data is collected."

        else ->
            "The dashboard is ready. Use Refresh now whenever you want a newer wellbeing snapshot."
    }
}

internal fun errorGuidanceMessage(
    message: String,
    showAuthenticateAction: Boolean,
    showResetVaultAction: Boolean
): String {
    return when {
        showResetVaultAction ->
            "This is a security-critical path. Reset the vault first, then authenticate again to rebuild a trusted state."

        guidanceIsSessionExpiredMessage(message) ->
            "Your protected session expired. Authenticate again to continue."

        guidanceIsSecurityDegradedMessage(message) ->
            "The trust state was downgraded. Authenticate again before protected flows continue."

        showAuthenticateAction ->
            "Try authenticating again first. If Health Connect still looks limited, open settings and retry."

        else ->
            "Review Health access in settings, then try recovery again."
    }
}

private fun guidanceHasMissingHealthPermissions(
    requiredCount: Int,
    grantedCount: Int
): Boolean {
    return requiredCount > 0 && grantedCount < requiredCount
}

private fun guidanceHasPermissionDeniedSignal(
    digitalTwinState: DigitalTwinState
): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED
}

private fun guidanceHasNoSignalData(
    digitalTwinState: DigitalTwinState
): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA
}

private fun guidanceIsSessionExpiredMessage(message: String): Boolean {
    return message.contains("session expired", ignoreCase = true)
}

private fun guidanceIsSecurityDegradedMessage(message: String): Boolean {
    return message.contains("security degraded", ignoreCase = true)
}

