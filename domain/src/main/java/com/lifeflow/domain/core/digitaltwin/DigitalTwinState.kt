package com.lifeflow.domain.core.digitaltwin

data class DigitalTwinState(
    val identityInitialized: Boolean,
    val stepsLast24h: Long?,
    val avgHeartRateLast24h: Long?,
    val lastUpdatedEpochMillis: Long
)