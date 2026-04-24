package com.lifeflow.security

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
}