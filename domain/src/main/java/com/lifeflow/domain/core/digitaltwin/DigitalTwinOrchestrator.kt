package com.lifeflow.domain.core.digitaltwin

class DigitalTwinOrchestrator(
    private val engine: DigitalTwinEngine
) {

    private var currentState: DigitalTwinState? = null

    @Synchronized
    fun refresh(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?,
        stepsPermissionGranted: Boolean? = null,
        heartRatePermissionGranted: Boolean? = null
    ): DigitalTwinState {

        val newState = engine.computeState(
            identityInitialized = identityInitialized,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h,
            stepsPermissionGranted = stepsPermissionGranted,
            heartRatePermissionGranted = heartRatePermissionGranted
        )

        currentState = newState
        return newState
    }

    @Synchronized
    fun getCurrentState(): DigitalTwinState? = currentState
}