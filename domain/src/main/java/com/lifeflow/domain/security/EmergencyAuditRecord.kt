package com.lifeflow.domain.security

enum class EmergencyAuditEventType {
    APPROVAL_SESSION_CREATED,
    ACTIVATION_ARTIFACT_ISSUED,
    ACTIVATION_ARTIFACT_CONSUMED,
    ACTIVATION_ARTIFACT_EXPIRED_UNUSED,
    ACTIVATION_ARTIFACT_REJECTED,
    EMERGENCY_WINDOW_CLEARED,
    EMERGENCY_WINDOW_EXPIRED
}

/**
 * Immutable audit payload for break-glass lifecycle events.
 *
 * This is the external-security-audit shape, independent from app logging.
 */
data class EmergencyAuditRecord(
    val timestampEpochMs: Long,
    val eventType: EmergencyAuditEventType,
    val approvalSessionId: String? = null,
    val artifactId: String? = null,
    val windowId: String? = null,
    val requestHash: String? = null,
    val reason: EmergencyAccessReason? = null,
    val trustedBaseOnly: Boolean? = null,
    val detail: String,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(timestampEpochMs >= 0L) {
            "timestampEpochMs must be >= 0."
        }
        require(detail.isNotBlank()) {
            "detail must not be blank."
        }
    }
}
