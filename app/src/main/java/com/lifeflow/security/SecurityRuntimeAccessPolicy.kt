package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog

internal enum class SecurityRuntimeAccessMode {
    STANDARD_PROTECTED,
    TRUSTED_BASE_READ
}

internal data class SecurityRuntimeAccessDecision(
    val allowed: Boolean,
    val effectiveTrustState: TrustState,
    val denialCode: String? = null
)

internal data class SecurityRuntimeAuthorizationRequest(
    val isReadOperation: Boolean,
    val requiresStrictAuth: Boolean,
    val trustedBaseOnly: Boolean,
    val hasRecentAuthentication: Boolean
)

internal enum class SecurityRuntimeAuthorizationOutcome {
    ALLOWED,
    DENIED,
    REQUIRES_ELEVATION,
    LOCKED,
    REQUIRES_EMERGENCY_RESOLUTION
}

internal data class SecurityRuntimeAuthorizationDecision(
    val outcome: SecurityRuntimeAuthorizationOutcome,
    val effectiveTrustState: TrustState,
    val code: String? = null
)

internal enum class SecurityRuntimeRuleActionOutcome {
    ALLOWED,
    DENIED,
    LOCKED
}

internal data class SecurityRuntimeRuleActionDecision(
    val outcome: SecurityRuntimeRuleActionOutcome,
    val effectiveTrustState: TrustState,
    val code: String? = null
)

private data class SecurityRuntimeAccessSnapshot(
    val containment: SecurityRuntimeContainmentSnapshot,
    val sessionAuthorized: Boolean,
    val hasTrustedBaseWindow: Boolean
)

internal object SecurityRuntimeAccessPolicy {

    fun decide(
        accessMode: SecurityRuntimeAccessMode
    ): SecurityRuntimeAccessDecision {
        val snapshot = currentSnapshot()

        val denialCode = when (accessMode) {
            SecurityRuntimeAccessMode.STANDARD_PROTECTED ->
                decideStandardProtected(snapshot)

            SecurityRuntimeAccessMode.TRUSTED_BASE_READ ->
                decideTrustedBaseRead(snapshot)
        }

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

    fun decideAuthorization(
        request: SecurityRuntimeAuthorizationRequest
    ): SecurityRuntimeAuthorizationDecision {
        val snapshot = currentSnapshot()

        containmentLockCode(snapshot)?.let { code ->
            return lockedAuthorizationDecision(
                snapshot = snapshot,
                code = code
            )
        }

        if (request.hasRecentAuthentication != snapshot.sessionAuthorized) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = "AUTH_CONTEXT_INVALID"
            )
        }

        if (snapshot.containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED) {
            return decideEmergencyLimitedAuthorization(
                snapshot = snapshot,
                request = request
            )
        }

        if (!snapshot.sessionAuthorized) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = "RECENT_AUTH_REQUIRED"
            )
        }

        if (!request.isReadOperation &&
            !snapshot.containment.capabilityEnvelope.allowSensitiveOperations
        ) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = "TRUST_NOT_SUFFICIENT"
            )
        }

        if (request.requiresStrictAuth &&
            snapshot.containment.effectiveTrustState != TrustState.VERIFIED
        ) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = "STRONGER_AUTH_REQUIRED"
            )
        }

        if (!request.isReadOperation &&
            snapshot.containment.effectiveTrustState == TrustState.DEGRADED
        ) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = "TRUST_NOT_SUFFICIENT"
            )
        }

        return allowedAuthorizationDecision(snapshot)
    }

    fun decideRuleAction(
        action: RuleAction
    ): SecurityRuntimeRuleActionDecision {
        val snapshot = currentSnapshot()

        containmentLockCode(snapshot)?.let { code ->
            return lockedRuleActionDecision(
                snapshot = snapshot,
                code = code
            )
        }

        if (snapshot.containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED) {
            return deniedRuleActionDecision(
                snapshot = snapshot,
                code = "EMERGENCY_LIMITED"
            )
        }

        if (!snapshot.sessionAuthorized) {
            return deniedRuleActionDecision(
                snapshot = snapshot,
                code = "AUTH_REQUIRED"
            )
        }

        if (!action.isRead &&
            !snapshot.containment.capabilityEnvelope.allowSensitiveOperations
        ) {
            return deniedRuleActionDecision(
                snapshot = snapshot,
                code = "TRUST_NOT_SUFFICIENT"
            )
        }

        if (!action.isRead &&
            snapshot.containment.effectiveTrustState == TrustState.DEGRADED
        ) {
            return deniedRuleActionDecision(
                snapshot = snapshot,
                code = "TRUST_NOT_SUFFICIENT"
            )
        }

        return allowedRuleActionDecision(snapshot)
    }

    private fun currentSnapshot(): SecurityRuntimeAccessSnapshot {
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

    private fun decideStandardProtected(
        snapshot: SecurityRuntimeAccessSnapshot
    ): String? {
        containmentLockCode(snapshot)?.let { return it }

        if (snapshot.containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED) {
            return "EMERGENCY_LIMITED"
        }

        if (!snapshot.sessionAuthorized) {
            return "AUTH_REQUIRED"
        }

        return null
    }

    private fun decideTrustedBaseRead(
        snapshot: SecurityRuntimeAccessSnapshot
    ): String? {
        containmentLockCode(snapshot)?.let { return it }

        if (snapshot.containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED &&
            !snapshot.hasTrustedBaseWindow
        ) {
            return "EMERGENCY_LIMITED"
        }

        return null
    }

    private fun decideEmergencyLimitedAuthorization(
        snapshot: SecurityRuntimeAccessSnapshot,
        request: SecurityRuntimeAuthorizationRequest
    ): SecurityRuntimeAuthorizationDecision {
        if (!request.trustedBaseOnly || !request.isReadOperation) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = "TRUSTED_BASE_ONLY_REQUIRED"
            )
        }

        if (!snapshot.sessionAuthorized) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = "EMERGENCY_APPROVAL_REQUIRED"
            )
        }

        if (!snapshot.hasTrustedBaseWindow) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = "EMERGENCY_APPROVAL_REQUIRED"
            )
        }

        return SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.REQUIRES_EMERGENCY_RESOLUTION,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = "RESOLVE_EMERGENCY_ACCESS"
        )
    }

    private fun containmentLockCode(
        snapshot: SecurityRuntimeAccessSnapshot
    ): String? {
        if (snapshot.containment.effectiveTrustState == TrustState.COMPROMISED) {
            return "COMPROMISED"
        }

        if (snapshot.containment.requireRecovery) {
            return "RECOVERY_REQUIRED"
        }

        if (!snapshot.containment.capabilityEnvelope.allowProtectedRuntime) {
            return "PROTECTED_RUNTIME_BLOCKED"
        }

        return null
    }

    private fun allowedAuthorizationDecision(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.ALLOWED,
            effectiveTrustState = snapshot.containment.effectiveTrustState
        )

    private fun deniedAuthorizationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: String
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.DENIED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun requiresElevationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: String
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.REQUIRES_ELEVATION,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun lockedAuthorizationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: String
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.LOCKED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun allowedRuleActionDecision(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeRuleActionDecision =
        SecurityRuntimeRuleActionDecision(
            outcome = SecurityRuntimeRuleActionOutcome.ALLOWED,
            effectiveTrustState = snapshot.containment.effectiveTrustState
        )

    private fun deniedRuleActionDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: String
    ): SecurityRuntimeRuleActionDecision =
        SecurityRuntimeRuleActionDecision(
            outcome = SecurityRuntimeRuleActionOutcome.DENIED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun lockedRuleActionDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: String
    ): SecurityRuntimeRuleActionDecision =
        SecurityRuntimeRuleActionDecision(
            outcome = SecurityRuntimeRuleActionOutcome.LOCKED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )
}
