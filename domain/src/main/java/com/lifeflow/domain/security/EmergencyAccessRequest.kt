package com.lifeflow.domain.security

/**
 * Domain-safe request for a temporary break-glass access attempt.
 */
data class EmergencyAccessRequest(
    val reason: EmergencyAccessReason,
    val requestedAtEpochMs: Long,
    val requestedDurationMs: Long
) {
    init {
        require(requestedAtEpochMs >= 0L) {
            "requestedAtEpochMs must be >= 0."
        }
        require(requestedDurationMs > 0L) {
            "requestedDurationMs must be > 0."
        }
    }
}
