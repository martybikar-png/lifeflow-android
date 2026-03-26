package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun loadingMessage(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        isAuthenticating ->
            "A protected session is active. LifeFlow is preparing your protected dashboard."

        healthState == HealthConnectUiState.NotInstalled ->
            "Health Connect is not installed yet. Install it first, then authenticate to continue."

        healthState == HealthConnectUiState.NotSupported ->
            "Health Connect is not supported on this device, so protected wellbeing refresh is unavailable."

        healthState == HealthConnectUiState.UpdateRequired ->
            "Health Connect needs an update before the first wellbeing snapshot can load."

        healthState == HealthConnectUiState.Available && requiredCount == 0 ->
            "Authentication is required. LifeFlow is still resolving the available wellbeing permission contract."

        requiredCount > 0 && grantedCount < requiredCount ->
            "Authentication is required, and some Health permissions are still missing for a fuller first snapshot."

        else ->
            "Authentication is required before the protected dashboard and first Digital Twin snapshot can load."
    }
}

internal fun healthSummaryMessage(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when (healthState) {
        HealthConnectUiState.Available -> {
            when {
                requiredCount == 0 ->
                    "Health Connect is available, but LifeFlow is still resolving which wellbeing permissions should be requested."

                grantedCount == 0 ->
                    "Health Connect is available, but no wellbeing permissions are currently granted."

                grantedCount < requiredCount ->
                    "Health Connect is available, but some required wellbeing permissions are still missing."

                else ->
                    "Health Connect is available and ready for protected dashboard refreshes."
            }
        }

        HealthConnectUiState.NotInstalled ->
            "Health Connect is not installed on this device."

        HealthConnectUiState.NotSupported ->
            "Health Connect is not supported on this device."

        HealthConnectUiState.UpdateRequired ->
            "Health Connect is installed, but an update is required before protected wellbeing refresh can continue."

        HealthConnectUiState.Unknown ->
            "Health Connect state is still being resolved."
    }
}

internal fun dashboardSummaryMessage(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Your dashboard is unlocked, but Health Connect must be ready before wellbeing data can fully populate."

        digitalTwinState == null ->
            "Your dashboard is unlocked. Load the first Digital Twin snapshot to populate the dashboard."

        requiredCount == 0 ->
            "Your dashboard is active, but LifeFlow is still resolving the wellbeing permission contract."

        requiredCount > 0 && grantedCount < requiredCount ->
            "Your dashboard is active, but missing permissions are limiting snapshot coverage."

        hasFullSignalCoverage(digitalTwinState) ->
            "Your dashboard is active and the latest Digital Twin snapshot is fully available."

        hasPermissionDeniedSignal(digitalTwinState) ->
            "Your dashboard is active, but some Digital Twin signals are blocked by missing permissions."

        hasNoSignalData(digitalTwinState) ->
            "Your dashboard is active, but there is not enough usable wellbeing data yet. Refresh later after more data is collected."

        else ->
            "Your dashboard is active and the latest Digital Twin snapshot is available with partial signal coverage."
    }
}

internal fun digitalTwinSummaryMessage(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "No Digital Twin snapshot has been loaded yet."
    }

    return when {
        hasFullSignalCoverage(digitalTwinState) ->
            "Your Digital Twin has both movement and heart-rate signals available."

        hasPermissionDeniedSignal(digitalTwinState) ->
            "Some Digital Twin signals are blocked because the required permission is missing."

        hasBlockedSignal(digitalTwinState) ->
            "The Digital Twin snapshot is blocked and cannot expose all signals right now."

        hasNoSignalData(digitalTwinState) ->
            "The Digital Twin is active, but there is no usable wellbeing data yet. Refresh later after more data is collected."

        hasUnknownSignalCoverage(digitalTwinState) ->
            "The Digital Twin snapshot exists, but its signal state is still being resolved."

        else ->
            "The Digital Twin snapshot is available, but one or more signals are still partial or pending."
    }
}

private fun hasFullSignalCoverage(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK
}

private fun hasPermissionDeniedSignal(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED
}

private fun hasBlockedSignal(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED
}

private fun hasNoSignalData(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA
}

private fun hasUnknownSignalCoverage(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.UNKNOWN &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.UNKNOWN
}
