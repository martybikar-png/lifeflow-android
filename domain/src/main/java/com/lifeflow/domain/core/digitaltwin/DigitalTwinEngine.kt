package com.lifeflow.domain.core.digitaltwin

class DigitalTwinEngine {

    fun computeState(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?,
        stepsPermissionGranted: Boolean? = null,
        heartRatePermissionGranted: Boolean? = null
    ): DigitalTwinState {

        val notes = mutableListOf<String>()

        // Fail-closed: if identity is not initialized/authenticated,
        // do not expose raw wellbeing metrics downstream.
        val blocked = !identityInitialized
        if (blocked) {
            notes += "Identity not initialized/authenticated: twin metrics are blocked."
        }

        val rawSteps = if (blocked) null else stepsLast24h
        val rawHeartRate = if (blocked) null else avgHeartRateLast24h

        val stepsAvailability = when {
            blocked -> DigitalTwinState.Availability.BLOCKED

            stepsPermissionGranted == false -> {
                notes += "Steps permission denied: metric cannot be accessed."
                DigitalTwinState.Availability.PERMISSION_DENIED
            }

            stepsPermissionGranted == null -> {
                notes += "Steps permission state unknown: metric not resolved yet."
                DigitalTwinState.Availability.UNKNOWN
            }

            rawSteps == null -> {
                notes += "Steps query completed but returned no usable data in the requested time range."
                DigitalTwinState.Availability.NO_DATA
            }

            rawSteps < 0L -> {
                notes += "Steps<0: invalid value treated as no-data."
                DigitalTwinState.Availability.NO_DATA
            }

            else -> DigitalTwinState.Availability.OK
        }

        val heartRateAvailability = when {
            blocked -> DigitalTwinState.Availability.BLOCKED

            heartRatePermissionGranted == false -> {
                notes += "Heart-rate permission denied: metric cannot be accessed."
                DigitalTwinState.Availability.PERMISSION_DENIED
            }

            heartRatePermissionGranted == null -> {
                notes += "Heart-rate permission state unknown: metric not resolved yet."
                DigitalTwinState.Availability.UNKNOWN
            }

            rawHeartRate == null -> {
                notes += "Heart-rate query completed but returned no usable data in the requested time range."
                DigitalTwinState.Availability.NO_DATA
            }

            rawHeartRate <= 0L -> {
                notes += "Heart-rate<=0: invalid value treated as no-data."
                DigitalTwinState.Availability.NO_DATA
            }

            else -> DigitalTwinState.Availability.OK
        }

        val safeSteps = when (stepsAvailability) {
            DigitalTwinState.Availability.OK -> rawSteps
            else -> null
        }

        val safeHeartRate = when (heartRateAvailability) {
            DigitalTwinState.Availability.OK -> rawHeartRate
            else -> null
        }

        return DigitalTwinState(
            identityInitialized = identityInitialized,
            stepsLast24h = safeSteps,
            avgHeartRateLast24h = safeHeartRate,
            lastUpdatedEpochMillis = System.currentTimeMillis(),
            stepsAvailability = stepsAvailability,
            heartRateAvailability = heartRateAvailability,
            notes = notes
        )
    }
}