package com.lifeflow.security

internal class SecurityRuntimeAuthorizationEvaluator(
    private val snapshotProvider: SecurityRuntimeAccessSnapshotProvider
) {
    fun decideAuthorization(
        request: SecurityRuntimeAuthorizationRequest
    ): SecurityRuntimeAuthorizationDecision {
        val snapshot = snapshotProvider.currentSnapshot()

        snapshot.containmentLockCode()?.let { code ->
            return lockedAuthorizationDecision(
                snapshot = snapshot,
                code = code
            )
        }

        if (request.hasRecentAuthentication != snapshot.sessionAuthorized) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.AUTH_CONTEXT_INVALID
            )
        }

        if (snapshot.isEmergencyLimited()) {
            return decideEmergencyLimitedAuthorization(
                snapshot = snapshot,
                request = request
            )
        }

        if (!snapshot.sessionAuthorized &&
            request.requiresTrustedBaseReadOnly() &&
            request.allowsTrustedBaseReadOnly()
        ) {
            return allowedAuthorizationDecision(snapshot)
        }

        if (!snapshot.sessionAuthorized) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.RECENT_AUTH_REQUIRED
            )
        }

        if (request.requiresSensitiveOperationsCapability() &&
            !snapshot.allowsSensitiveOperations()
        ) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT
            )
        }

        if (request.requiresVerifiedTrust() &&
            !snapshot.isVerified()
        ) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.STRONGER_AUTH_REQUIRED
            )
        }

        if (request.requiresSensitiveOperationsCapability() &&
            snapshot.isDegraded()
        ) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT
            )
        }

        return allowedAuthorizationDecision(snapshot)
    }

    private fun decideEmergencyLimitedAuthorization(
        snapshot: SecurityRuntimeAccessSnapshot,
        request: SecurityRuntimeAuthorizationRequest
    ): SecurityRuntimeAuthorizationDecision {
        if (!request.requiresTrustedBaseReadOnly() ||
            !request.allowsTrustedBaseReadOnly()
        ) {
            return deniedAuthorizationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.TRUSTED_BASE_ONLY_REQUIRED
            )
        }

        if (!snapshot.sessionAuthorized) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.EMERGENCY_APPROVAL_REQUIRED
            )
        }

        if (!snapshot.hasTrustedBaseWindow) {
            return requiresElevationDecision(
                snapshot = snapshot,
                code = SecurityRuntimeDecisionCode.EMERGENCY_APPROVAL_REQUIRED
            )
        }

        return SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.REQUIRES_EMERGENCY_RESOLUTION,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = SecurityRuntimeDecisionCode.RESOLVE_EMERGENCY_ACCESS
        )
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
        code: SecurityRuntimeDecisionCode
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.DENIED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun requiresElevationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: SecurityRuntimeDecisionCode
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.REQUIRES_ELEVATION,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun lockedAuthorizationDecision(
        snapshot: SecurityRuntimeAccessSnapshot,
        code: SecurityRuntimeDecisionCode
    ): SecurityRuntimeAuthorizationDecision =
        SecurityRuntimeAuthorizationDecision(
            outcome = SecurityRuntimeAuthorizationOutcome.LOCKED,
            effectiveTrustState = snapshot.containment.effectiveTrustState,
            code = code
        )

    private fun SecurityRuntimeAuthorizationRequest.requiresVerifiedTrust(): Boolean =
        authorizationPolicy.requiresVerifiedTrust

    private fun SecurityRuntimeAuthorizationRequest.requiresTrustedBaseReadOnly(): Boolean =
        authorizationPolicy.requiresTrustedBaseReadOnly

    private fun SecurityRuntimeAuthorizationRequest.requiresSensitiveOperationsCapability(): Boolean =
        operation.securityClass.requiresSensitiveOperationsCapability

    private fun SecurityRuntimeAuthorizationRequest.allowsTrustedBaseReadOnly(): Boolean =
        operation.securityClass.allowsTrustedBaseReadOnly
}