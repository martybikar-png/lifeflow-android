package com.lifeflow.domain.security

/**
 * Active temporary emergency access window.
 *
 * This model is domain-safe and does not assume any storage or UI implementation.
 */
data class EmergencyAccessWindow(
    val windowId: String,
    val reason: EmergencyAccessReason,
    val startedAtEpochMs: Long,
    val expiresAtEpochMs: Long,
    val trustedBaseOnly: Boolean
) {
    init {
        require(windowId.isNotBlank()) {
            "windowId must not be blank."
        }
        require(startedAtEpochMs >= 0L) {
            "startedAtEpochMs must be >= 0."
        }
        require(expiresAtEpochMs > startedAtEpochMs) {
            "expiresAtEpochMs must be greater than startedAtEpochMs."
        }
    }

    fun isActiveAt(epochMs: Long): Boolean {
        return epochMs in startedAtEpochMs until expiresAtEpochMs
    }
}
