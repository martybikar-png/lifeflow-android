package com.lifeflow.domain.core.digitaltwin

data class DigitalTwinState(
    val identityInitialized: Boolean,
    val stepsLast24h: Long?,
    val avgHeartRateLast24h: Long?,
    val lastUpdatedEpochMillis: Long,

    // --- Phase 1: precise availability semantics + audit notes ---
    val stepsAvailability: Availability = Availability.UNKNOWN,
    val heartRateAvailability: Availability = Availability.UNKNOWN,
    val notes: List<String> = emptyList()
) {
    enum class Availability {
        /**
         * We do not know yet.
         * Examples:
         * - refresh has not run yet
         * - provider status is not resolved yet
         * - query/result is not available yet
         */
        UNKNOWN,

        /**
         * Data is present and considered usable.
         */
        OK,

        /**
         * Required permission is not granted, so the metric cannot be accessed.
         */
        PERMISSION_DENIED,

        /**
         * Access/query path was allowed and completed, but no usable data
         * was returned for the requested time range.
         */
        NO_DATA,

        /**
         * Metric is intentionally blocked by security state / identity gate.
         * Fail-closed condition.
         */
        BLOCKED
    }
}