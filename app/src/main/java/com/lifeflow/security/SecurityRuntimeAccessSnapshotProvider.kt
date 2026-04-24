package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class SecurityRuntimeAccessSnapshotProvider {
    fun currentSnapshot(): SecurityRuntimeAccessSnapshot {
        refreshRuntimeHardeningTrustState()

        val currentTrustState = SecurityRuleEngine.getTrustState()

        return SecurityRuntimeAccessSnapshot(
            containment = SecurityAuditLog.runtimeContainmentSnapshot(
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