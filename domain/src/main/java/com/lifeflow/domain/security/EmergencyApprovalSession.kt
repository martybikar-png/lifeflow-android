package com.lifeflow.domain.security

/**
 * Canonical dual-control approval record for break-glass.
 *
 * This is the immutable context created in the approval phase.
 * Activation artifacts must reference this session instead of inventing
 * their own free-form reason/scope context.
 */
data class EmergencyApprovalSession(
    val sessionId: String,
    val requestHash: String,
    val reason: EmergencyAccessReason,
    val requestedAtEpochMs: Long,
    val approvedAtEpochMs: Long,
    val approvedWindowDurationMs: Long,
    val trustedBaseOnly: Boolean,
    val degradedCauseSnapshotId: String,
    val firstApproverId: String,
    val secondApproverId: String
) {
    init {
        require(sessionId.isNotBlank()) {
            "sessionId must not be blank."
        }
        require(requestHash.isNotBlank()) {
            "requestHash must not be blank."
        }
        require(requestedAtEpochMs >= 0L) {
            "requestedAtEpochMs must be >= 0."
        }
        require(approvedAtEpochMs >= requestedAtEpochMs) {
            "approvedAtEpochMs must be >= requestedAtEpochMs."
        }
        require(approvedWindowDurationMs > 0L) {
            "approvedWindowDurationMs must be > 0."
        }
        require(degradedCauseSnapshotId.isNotBlank()) {
            "degradedCauseSnapshotId must not be blank."
        }
        require(firstApproverId.isNotBlank()) {
            "firstApproverId must not be blank."
        }
        require(secondApproverId.isNotBlank()) {
            "secondApproverId must not be blank."
        }
        require(firstApproverId != secondApproverId) {
            "Dual control requires two distinct approvers."
        }
    }
}
