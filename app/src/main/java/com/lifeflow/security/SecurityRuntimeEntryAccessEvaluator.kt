package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal class SecurityRuntimeEntryAccessEvaluator(
    private val snapshotProvider: SecurityRuntimeAccessSnapshotProvider
) {
    fun decideStandardProtectedEntry(): SecurityRuntimeAccessDecision {
        val snapshot = snapshotProvider.currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideStandardProtected(snapshot)
        )
    }

    fun decideTrustedBaseReadEntry(): SecurityRuntimeAccessDecision {
        val snapshot = snapshotProvider.currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideTrustedBaseRead(snapshot)
        )
    }

    fun decideBiometricAuthenticationHandoff(): SecurityRuntimeAccessDecision {
        val snapshot = snapshotProvider.currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideBiometricAuthenticationHandoff(snapshot)
        )
    }

    fun decideBiometricBootstrapOperation(
        operation: DomainOperation
    ): SecurityRuntimeAccessDecision {
        val snapshot = snapshotProvider.currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideBiometricBootstrapOperation(
                snapshot = snapshot,
                operation = operation
            )
        )
    }

    private fun accessDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        denialCode: SecurityRuntimeDecisionCode?
    ): SecurityRuntimeAccessDecision {
        return if (denialCode == null) {
            SecurityRuntimeAccessDecision(
                allowed = true,
                effectiveTrustState = snapshot.containment.effectiveTrustState
            )
        } else {
            SecurityRuntimeAccessDecision(
                allowed = false,
                effectiveTrustState = snapshot.containment.effectiveTrustState,
                denialCode = denialCode
            )
        }
    }

    private fun decideStandardProtected(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeDecisionCode? {
        snapshot.containmentLockCode()?.let { return it }

        if (snapshot.isEmergencyLimited()) {
            return SecurityRuntimeDecisionCode.EMERGENCY_LIMITED
        }

        if (!snapshot.sessionAuthorized) {
            return SecurityRuntimeDecisionCode.AUTH_REQUIRED
        }

        return null
    }

    private fun decideTrustedBaseRead(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeDecisionCode? {
        snapshot.containmentLockCode()?.let { return it }

        if (snapshot.isEmergencyLimited() &&
            !snapshot.hasTrustedBaseWindow
        ) {
            return SecurityRuntimeDecisionCode.EMERGENCY_LIMITED
        }

        return null
    }

    private fun decideBiometricAuthenticationHandoff(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeDecisionCode? {
        if (!snapshot.sessionAuthorized) {
            return SecurityRuntimeDecisionCode.AUTH_REQUIRED
        }

        if (snapshot.isEmergencyLimited()) {
            return SecurityRuntimeDecisionCode.EMERGENCY_LIMITED
        }

        if (snapshot.containment.capabilityEnvelope.allowRecoveryFlow) {
            return null
        }

        if (snapshot.containment.effectiveTrustState == TrustState.COMPROMISED) {
            return SecurityRuntimeDecisionCode.COMPROMISED
        }

        if (snapshot.containment.requireRecovery) {
            return SecurityRuntimeDecisionCode.RECOVERY_REQUIRED
        }

        if (!snapshot.containment.capabilityEnvelope.allowProtectedRuntime) {
            return SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
        }

        return null
    }

    private fun decideBiometricBootstrapOperation(
        snapshot: SecurityRuntimeAccessSnapshot,
        operation: DomainOperation
    ): SecurityRuntimeDecisionCode? {
        when (snapshot.containment.effectiveTrustState) {
            TrustState.COMPROMISED ->
                return SecurityRuntimeDecisionCode.COMPROMISED

            TrustState.EMERGENCY_LIMITED ->
                return SecurityRuntimeDecisionCode.EMERGENCY_LIMITED

            TrustState.DEGRADED,
            TrustState.VERIFIED -> Unit
        }

        if (snapshot.containment.requireRecovery) {
            return SecurityRuntimeDecisionCode.RECOVERY_REQUIRED
        }

        if (!snapshot.sessionAuthorized) {
            return SecurityRuntimeDecisionCode.AUTH_REQUIRED
        }

        return when (operation) {
            DomainOperation.READ_ACTIVE_IDENTITY ->
                null

            DomainOperation.SAVE_IDENTITY ->
                if (snapshot.containment.capabilityEnvelope.allowStateMutation) {
                    null
                } else {
                    SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
                }

            else ->
                SecurityRuntimeDecisionCode.AUTH_REQUIRED
        }
    }
}
