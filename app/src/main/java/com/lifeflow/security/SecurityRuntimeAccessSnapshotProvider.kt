package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class SecurityRuntimeAccessSnapshotProvider {
    fun currentSnapshot(): SecurityRuntimeAccessSnapshot {
        refreshRuntimeHardeningTrustState()

        val currentTrustState = SecurityRuleEngine.getTrustState()
        val trustStateLastTransitionAt =
            SecurityRuleEngine.getTrustStateLastTransitionAt()

        return SecurityRuntimeAccessSnapshot(
            containment = SecurityAuditLog.runtimeContainmentSnapshotSince(
                since = trustStateLastTransitionAt,
                currentTrustState = currentTrustState
            ),
            sessionAuthorized = SecurityAccessSession.isAuthorized(),
            hasTrustedBaseWindow = SecurityEmergencyWindowStatePortAdapter
                .hasActiveTrustedBaseWindow()
        )
    }

    private fun refreshRuntimeHardeningTrustState() {
        if (SecurityHardeningGuard.isCompromisedQuick()) {
            SecurityRuleEngine.reportRuntimeCompromise(
                reason = "Quick runtime hardening signal detected during access evaluation."
            )
        }
    }
}
