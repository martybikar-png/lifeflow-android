package com.lifeflow.domain.security

/**
 * Domain boundary contract for compromise, lockout, and recovery flows.
 *
 * This port exposes the lifecycle side of security posture management without
 * leaking infrastructure-specific implementation details into domain logic.
 */
interface SecurityLifecyclePort {
    fun onCompromised(reason: CompromiseReason)
    fun onLockout(context: LockoutContext)
    fun recoveryOptions(): List<RecoveryOption>

    /**
     * Creates the immutable dual-control approval context for break-glass.
     */
    fun createEmergencyApprovalSession(
        request: EmergencyAccessRequest,
        degradedCauseSnapshotId: String,
        firstApproverId: String,
        secondApproverId: String
    ): EmergencyApprovalSession

    /**
     * Issues the short-lived sender-constrained activation artifact.
     */
    fun issueEmergencyActivationArtifact(
        request: EmergencyActivationRequest
    ): EmergencyActivationArtifact

    /**
     * Activates the approved emergency window from a valid activation artifact.
     */
    fun activateEmergencyAccess(
        artifact: EmergencyActivationArtifact
    ): EmergencyAccessWindow

    /**
     * Clears an active emergency access window.
     */
    fun clearEmergencyAccess()
}
