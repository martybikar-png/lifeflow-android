package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal object SecurityRuntimeOperationGate {

    fun evaluate(
        operation: DomainOperation
    ): SecurityRuntimeOperationDecision =
        SecurityRuntimeAccessPolicy.decideOperation(operation)

    fun denialCodeName(
        decision: SecurityRuntimeOperationDecision
    ): String =
        decision.code?.name ?: when (decision.outcome) {
            SecurityRuntimeOperationOutcome.ALLOWED -> "ALLOWED"
            SecurityRuntimeOperationOutcome.DENIED -> "DENIED"
            SecurityRuntimeOperationOutcome.LOCKED -> "LOCKED"
        }

    fun isCompromisedLockdown(
        decision: SecurityRuntimeOperationDecision
    ): Boolean =
        decision.outcome == SecurityRuntimeOperationOutcome.LOCKED &&
            decision.code == SecurityRuntimeDecisionCode.COMPROMISED

    fun lockedReason(
        decision: SecurityRuntimeOperationDecision,
        detail: String
    ): String =
        decision.code.toSecurityLockedReason().withDetail(detail)

    fun deniedException(
        operation: DomainOperation,
        decision: SecurityRuntimeOperationDecision,
        reason: String
    ): SecurityLockedException =
        SecurityLockedException(
            lockedReason = lockedReason(
                decision = decision,
                detail = reason
            )
        )

    private fun SecurityRuntimeDecisionCode?.toSecurityLockedReason():
        SecurityLockedReason =
        when (this) {
            SecurityRuntimeDecisionCode.COMPROMISED ->
                SecurityLockedReason.COMPROMISED

            SecurityRuntimeDecisionCode.RECOVERY_REQUIRED ->
                SecurityLockedReason.RECOVERY_REQUIRED

            SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED ->
                SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED

            SecurityRuntimeDecisionCode.EMERGENCY_LIMITED,
            SecurityRuntimeDecisionCode.TRUSTED_BASE_ONLY_REQUIRED,
            SecurityRuntimeDecisionCode.EMERGENCY_NOT_APPROVED,
            SecurityRuntimeDecisionCode.EMERGENCY_WINDOW_EXPIRED,
            SecurityRuntimeDecisionCode.EMERGENCY_APPROVAL_REQUIRED,
            SecurityRuntimeDecisionCode.EMERGENCY_REJECTED,
            SecurityRuntimeDecisionCode.RESOLVE_EMERGENCY_ACCESS ->
                SecurityLockedReason.EMERGENCY_LIMITED

            SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT,
            SecurityRuntimeDecisionCode.AUTH_CONTEXT_INVALID,
            SecurityRuntimeDecisionCode.STRONGER_AUTH_REQUIRED,
            SecurityRuntimeDecisionCode.RECENT_AUTH_REQUIRED,
            SecurityRuntimeDecisionCode.AUTH_REQUIRED,
            null ->
                SecurityLockedReason.AUTH_REQUIRED
        }
}
