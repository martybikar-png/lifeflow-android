package com.lifeflow

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun hasFullCardSignalCoverage(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK
}

internal fun hasPermissionDeniedCardSignal(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED
}

internal fun hasBlockedCardSignal(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED
}

internal fun hasNoCardSignalData(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA
}

internal fun hasUnknownCardSignalCoverage(digitalTwinState: DigitalTwinState): Boolean {
    return digitalTwinState.stepsAvailability == DigitalTwinState.Availability.UNKNOWN &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.UNKNOWN
}

internal fun countAvailableCardSignals(digitalTwinState: DigitalTwinState): Int {
    var availableSignals = 0

    if (digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    if (digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    return availableSignals
}