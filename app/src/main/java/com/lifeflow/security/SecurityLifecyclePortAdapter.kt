package com.lifeflow.security

import com.lifeflow.domain.security.CompromiseReason
import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.LockReason
import com.lifeflow.domain.security.LockoutContext
import com.lifeflow.domain.security.RecoveryOption
import com.lifeflow.domain.security.SecurityLifecyclePort

class SecurityLifecyclePortAdapter : SecurityLifecyclePort {

    override fun onCompromised(reason: CompromiseReason) {
        clearSessionAndTransition(
            targetState = TrustState.COMPROMISED,
            reason = "Lifecycle compromise: ${reason.name}"
        )
    }

    override fun onLockout(context: LockoutContext) {
        clearSessionAndTransition(
            targetState = lockoutTargetState(context.reason),
            reason = lifecycleLockoutReason(context)
        )
    }

    override fun recoveryOptions(): List<RecoveryOption> =
        recoveryOptionsFor(currentTrustState())

    override fun createEmergencyApprovalSession(
        request: EmergencyAccessRequest,
        degradedCauseSnapshotId: String,
        firstApproverId: String,
        secondApproverId: String
    ): EmergencyApprovalSession {
        return SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = degradedCauseSnapshotId,
            firstApproverId = firstApproverId,
            secondApproverId = secondApproverId
        )
    }

    override fun issueEmergencyActivationArtifact(
        request: EmergencyActivationRequest
    ): EmergencyActivationArtifact {
        return SecurityEmergencyAccessAuthority.issueActivationArtifact(request)
    }

    override fun activateEmergencyAccess(
        artifact: EmergencyActivationArtifact
    ): EmergencyAccessWindow {
        return SecurityEmergencyAccessAuthority.activate(artifact)
    }

    override fun clearEmergencyAccess() {
        SecurityEmergencyAccessAuthority.clear(
            reason = "Lifecycle clearEmergencyAccess"
        )
    }

    private fun clearSessionAndTransition(
        targetState: TrustState,
        reason: String
    ) {
        SecurityAccessSession.clear()
        SecurityRuleEngine.setTrustState(
            targetState,
            reason = reason
        )
    }

    private fun lockoutTargetState(
        reason: LockReason
    ): TrustState =
        when (reason) {
            LockReason.COMPROMISED,
            LockReason.RECOVERY_REQUIRED -> TrustState.COMPROMISED

            LockReason.LOCKED_OUT,
            LockReason.EMERGENCY_REJECTED -> TrustState.DEGRADED
        }

    private fun lifecycleLockoutReason(
        context: LockoutContext
    ): String =
        buildString {
            append("Lifecycle lockout: ")
            append(context.reason.name)
            context.attemptedOperation?.let {
                append(" during ")
                append(it.name)
            }
        }

    private fun recoveryOptionsFor(
        trustState: TrustState
    ): List<RecoveryOption> =
        when (trustState) {
            TrustState.VERIFIED -> emptyList()

            TrustState.DEGRADED -> listOf(
                RecoveryOption.RETRY_AUTHENTICATION,
                RecoveryOption.RESTART_SECURE_SESSION,
                RecoveryOption.ENTER_BREAK_GLASS
            )

            TrustState.EMERGENCY_LIMITED -> listOf(
                RecoveryOption.EXIT_BREAK_GLASS
            )

            TrustState.COMPROMISED -> listOf(
                RecoveryOption.RESET_VAULT,
                RecoveryOption.CONTACT_SUPPORT
            )
        }

    private fun currentTrustState(): TrustState =
        SecurityRuleEngine.getTrustState()
}
