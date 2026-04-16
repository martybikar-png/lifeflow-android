package com.lifeflow.domain.security

/**
 * Short-lived activation artifact for replay-resistant break-glass entry.
 *
 * This is NOT the emergency window itself.
 * It is the sender-constrained artifact that can be used once to activate
 * the approved emergency window.
 */
data class EmergencyActivationArtifact(
    val artifactId: String,
    val approvalSessionId: String,
    val requestHash: String,
    val reason: EmergencyAccessReason,
    val audience: String,
    val nonce: String,
    val issuedAtEpochMs: Long,
    val notBeforeEpochMs: Long,
    val expiresAtEpochMs: Long,
    val approvedWindowDurationMs: Long,
    val trustedBaseOnly: Boolean,
    val keyBinding: EmergencyActivationKeyBinding
) {
    init {
        require(artifactId.isNotBlank()) {
            "artifactId must not be blank."
        }
        require(approvalSessionId.isNotBlank()) {
            "approvalSessionId must not be blank."
        }
        require(requestHash.isNotBlank()) {
            "requestHash must not be blank."
        }
        require(audience.isNotBlank()) {
            "audience must not be blank."
        }
        require(nonce.isNotBlank()) {
            "nonce must not be blank."
        }
        require(issuedAtEpochMs >= 0L) {
            "issuedAtEpochMs must be >= 0."
        }
        require(notBeforeEpochMs >= issuedAtEpochMs) {
            "notBeforeEpochMs must be >= issuedAtEpochMs."
        }
        require(expiresAtEpochMs > notBeforeEpochMs) {
            "expiresAtEpochMs must be > notBeforeEpochMs."
        }
        require(approvedWindowDurationMs > 0L) {
            "approvedWindowDurationMs must be > 0."
        }
    }

    fun isUsableAt(epochMs: Long): Boolean {
        return epochMs in notBeforeEpochMs..expiresAtEpochMs
    }
}
