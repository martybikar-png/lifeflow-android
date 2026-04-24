package com.lifeflow.security

import com.lifeflow.domain.security.AuthorizationPolicy
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal data class SecurityRuntimeAccessDecision(
    val allowed: Boolean,
    val effectiveTrustState: TrustState,
    val denialCode: SecurityRuntimeDecisionCode? = null
)

internal data class SecurityRuntimeAuthorizationRequest(
    val operation: DomainOperation,
    val authorizationPolicy: AuthorizationPolicy,
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
    val code: SecurityRuntimeDecisionCode? = null
)

internal enum class SecurityRuntimeOperationOutcome {
    ALLOWED,
    DENIED,
    LOCKED
}

internal data class SecurityRuntimeOperationDecision(
    val outcome: SecurityRuntimeOperationOutcome,
    val effectiveTrustState: TrustState,
    val code: SecurityRuntimeDecisionCode? = null
)

private data class SecurityRuntimeAccessSnapshot(
    val containment: SecurityRuntimeContainmentSnapshot,
    val sessionAuthorized: Boolean,
    val hasTrustedBaseWindow: Boolean
)

internal object SecurityRuntimeAccessPolicy {

    fun decideStandardProtectedEntry(): SecurityRuntimeAccessDecision {
        val snapshot = currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideStandardProtected(snapshot)
        )
    }

    fun decideTrustedBaseReadEntry(): SecurityRuntimeAccessDecision {
        val snapshot = currentSnapshot()
        return accessDecision(
            snapshot = snapshot,
            denialCode = decideTrustedBaseRead(snapshot)
        )
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

    fun decideOperation(
        operation: DomainOperation
    ): SecurityRuntimeOperationDecision {
        val snapshot = currentSnapshot()

        containmentLockCode(snapshot)?.let { code ->
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

    private fun currentSnapshot(): SecurityRuntimeAccessSnapshot {
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
        containmentLockCode(snapshot)?.let { return it }

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
        containmentLockCode(snapshot)?.let { return it }

        if (snapshot.isEmergencyLimited() &&
            !snapshot.hasTrustedBaseWindow
        ) {
            return SecurityRuntimeDecisionCode.EMERGENCY_LIMITED
        }

        return null
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

    private fun containmentLockCode(
        snapshot: SecurityRuntimeAccessSnapshot
    ): SecurityRuntimeDecisionCode? {
        if (snapshot.isCompromised()) {
            return SecurityRuntimeDecisionCode.COMPROMISED
        }

        if (snapshot.requiresRecovery()) {
            return SecurityRuntimeDecisionCode.RECOVERY_REQUIRED
        }

        if (!snapshot.allowsProtectedRuntime()) {
            return SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
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

    private fun SecurityRuntimeAuthorizationRequest.requiresVerifiedTrust(): Boolean =
        authorizationPolicy.requiresVerifiedTrust

    private fun SecurityRuntimeAuthorizationRequest.requiresTrustedBaseReadOnly(): Boolean =
        authorizationPolicy.requiresTrustedBaseReadOnly

    private fun SecurityRuntimeAuthorizationRequest.requiresSensitiveOperationsCapability(): Boolean =
        operation.securityClass.requiresSensitiveOperationsCapability

    private fun SecurityRuntimeAuthorizationRequest.allowsTrustedBaseReadOnly(): Boolean =
        operation.securityClass.allowsTrustedBaseReadOnly

    private fun DomainOperation.requiresSensitiveOperationsCapability(): Boolean =
        securityClass.requiresSensitiveOperationsCapability

    private fun SecurityRuntimeAccessSnapshot.isVerified(): Boolean =
        containment.effectiveTrustState == TrustState.VERIFIED

    private fun SecurityRuntimeAccessSnapshot.isDegraded(): Boolean =
        containment.effectiveTrustState == TrustState.DEGRADED

    private fun SecurityRuntimeAccessSnapshot.isEmergencyLimited(): Boolean =
        containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED

    private fun SecurityRuntimeAccessSnapshot.isCompromised(): Boolean =
        containment.effectiveTrustState == TrustState.COMPROMISED

    private fun SecurityRuntimeAccessSnapshot.requiresRecovery(): Boolean =
        containment.requireRecovery

    private fun SecurityRuntimeAccessSnapshot.allowsProtectedRuntime(): Boolean =
        containment.capabilityEnvelope.allowProtectedRuntime

    private fun SecurityRuntimeAccessSnapshot.allowsSensitiveOperations(): Boolean =
        containment.capabilityEnvelope.allowSensitiveOperations
}

internal fun SecurityRuntimeAccessDecision.toLockedReason(
    detail: String
): String? {
    if (allowed) return null

    return when (denialCode) {
        SecurityRuntimeDecisionCode.COMPROMISED ->
            SecurityLockedReason.COMPROMISED.withDetail(detail)

        SecurityRuntimeDecisionCode.RECOVERY_REQUIRED ->
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail(detail)

        SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED ->
            SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED.withDetail(detail)

        SecurityRuntimeDecisionCode.EMERGENCY_LIMITED ->
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail(detail)

        SecurityRuntimeDecisionCode.AUTH_REQUIRED,
        null ->
            SecurityLockedReason.AUTH_REQUIRED.withDetail(detail)

        else ->
            SecurityLockedReason.AUTH_REQUIRED.withDetail(detail)
    }
}

internal fun SecurityRuntimeAccessDecision.toFailureMessage(): String =
    toStandardProtectedUserMessage()
