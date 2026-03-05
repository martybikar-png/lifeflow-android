package com.lifeflow.domain.core.digitaltwin

class DigitalTwinEngine {

    fun computeState(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?
    ): DigitalTwinState {
        return DigitalTwinState(
            identityInitialized = identityInitialized,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        )
    }
}