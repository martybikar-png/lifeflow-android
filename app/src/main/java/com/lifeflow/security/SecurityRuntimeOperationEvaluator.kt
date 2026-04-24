package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal class SecurityRuntimeOperationEvaluator(
    private val snapshotProvider: SecurityRuntimeAccessSnapshotProvider
) {
    fun decideOperation(
        operation: DomainOperation
    ): SecurityRuntimeOperationDecision {
        val snapshot = snapshotProvider.currentSnapshot()

        snapshot.containmentLockCode()?.let { code ->
            return lockedOperationDecision(
                snapshot = snapshot,
                code = code
            )
        }

        if (snapshot.isEmergencyLimited()) {
            return deniedOperationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.EMERGENCY_LIMITED
            )
        }

        if (!snapshot.sessionAuthorized) {
            return deniedOperationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.AUTH_REQUIRED
            )
        }

        if (operation.requiresSensitiveOperationsCapability() &&
            !snapshot.allowsSensitiveOperations()
        ) {
            return deniedOperationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT
            )
        }

        if (operation.requiresSensitiveOperationsCapability() &&
            snapshot.isDegraded()
        ) {
            return deniedOperationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT
            )
        }

        return allowedOperationDecision(snapshot)
    }

    private fun allowedOperationDecision(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeOperationDecision =
        SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.ALLOWED,
            effectiveTrustState = snapshot.containment.effectiveTrustState
        )

    private fun deniedOperationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: SecurityRuntimeDecisionCode
    ): SecurityRuntimeOperationDecision =
        SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.DENIED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun lockedOperationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: SecurityRuntimeDecisionCode
    ): SecurityRuntimeOperationDecision =
        SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.LOCKED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun DomainOperation.requiresSensitiveOperationsCapability(): Boolean =
        securityClass.requiresSensitiveOperationsCapability
}