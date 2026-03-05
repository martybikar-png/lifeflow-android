package com.lifeflow.domain.core.digitaltwin

class DigitalTwinOrchestrator(
    private val engine: DigitalTwinEngine
) {

    private var currentState: DigitalTwinState? = null

    fun refresh(
        identityInitialized: Boolean,
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?
    ): DigitalTwinState {

        val newState = engine.computeState(
            identityInitialized = identityInitialized,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h
        )

        currentState = newState
        return newState
    }

    fun getCurrentState(): DigitalTwinState? = currentState
}