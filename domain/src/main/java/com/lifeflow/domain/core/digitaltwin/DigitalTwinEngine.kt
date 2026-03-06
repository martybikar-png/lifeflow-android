package com.lifeflow.domain.core.digitaltwin

class DigitalTwinEngine {

    fun computeState(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?
    ): DigitalTwinState {

        val notes = mutableListOf<String>()

        // Fail-closed: if identity is not initialized/authenticated,
        // do not expose raw wellbeing metrics downstream.
        val blocked = !identityInitialized
        val safeSteps = if (blocked) null else stepsLast24h
        val safeHeartRate = if (blocked) null else avgHeartRateLast24h

        if (blocked) {
            notes += "Identity not initialized/authenticated: twin metrics are blocked."
        }

        val stepsAvailability = when {
            blocked -> DigitalTwinState.Availability.BLOCKED
            safeSteps == null -> DigitalTwinState.Availability.UNKNOWN
            safeSteps == 0L -> {
                notes += "Steps=0 in last 24h: either truly 0 or provider hasn't synced yet."
                DigitalTwinState.Availability.NO_DATA
            }
            else -> DigitalTwinState.Availability.OK
        }

        val heartRateAvailability = when {
            blocked -> DigitalTwinState.Availability.BLOCKED
            safeHeartRate == null -> {
                notes += "HR is null: often means no HR records available (no watch/band or no measurements in last 24h)."
                DigitalTwinState.Availability.NO_DATA
            }
            safeHeartRate <= 0L -> {
                notes += "HR<=0: treated as no-data/invalid."
                DigitalTwinState.Availability.NO_DATA
            }
            else -> DigitalTwinState.Availability.OK
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