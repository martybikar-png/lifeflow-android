package com.lifeflow.domain.core.digitaltwin

data class DigitalTwinState(
    val identityInitialized: Boolean,
    val stepsLast24h: Long?,
    val avgHeartRateLast24h: Long?,
    val lastUpdatedEpochMillis: Long,

    // --- Added: Data availability + audit notes (non-breaking additive fields) ---
    val stepsAvailability: Availability = Availability.UNKNOWN,
    val heartRateAvailability: Availability = Availability.UNKNOWN,
    val notes: List<String> = emptyList()
) {
    enum class Availability {
        /**
         * We don't know yet (HC not queried, permissions unknown, provider delay, etc.)
         */
        UNKNOWN,

        /**
         * Data is present and considered usable.
         */
        OK,

        /**
         * Data query succeeded but returned empty/none within the requested time range.
         * (e.g., no HR records in last 24h, or steps not synced yet)
         */
        NO_DATA,

        /**
         * Identity not initialized / app not authenticated (depending on how you interpret gate).
         * We keep this as a "why" flag for the UI/diagnostics layer.
         */
        BLOCKED
    }
}