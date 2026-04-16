package com.lifeflow.domain.security

/**
 * Domain-safe request for issuing a short-lived activation artifact.
 *
 * This is separate from the approval phase:
 * - approval session = dual-control decision context
 * - activation request = short-lived sender-constrained activation step
 */
data class EmergencyActivationRequest(
    val approvalSession: EmergencyApprovalSession,
    val audience: String,
    val nonce: String,
    val requestedAtEpochMs: Long,
    val artifactLifetimeMs: Long,
    val keyBinding: EmergencyActivationKeyBinding
) {
    init {
        require(audience.isNotBlank()) {
            "audience must not be blank."
        }
        require(nonce.isNotBlank()) {
            "nonce must not be blank."
        }
        require(requestedAtEpochMs >= 0L) {
            "requestedAtEpochMs must be >= 0."
        }
        require(artifactLifetimeMs > 0L) {
            "artifactLifetimeMs must be > 0."
        }
    }
}
