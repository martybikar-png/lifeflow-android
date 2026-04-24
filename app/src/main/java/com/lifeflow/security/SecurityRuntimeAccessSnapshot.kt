package com.lifeflow.security

internal data class SecurityRuntimeAccessSnapshot(
    val containment: SecurityRuntimeContainmentSnapshot,
    val sessionAuthorized: Boolean,
    val hasTrustedBaseWindow: Boolean
)

internal fun SecurityRuntimeAccessSnapshot.containmentLockCode(): SecurityRuntimeDecisionCode? {
    if (isCompromised()) {
        return SecurityRuntimeDecisionCode.COMPROMISED
    }

    if (requiresRecovery()) {
        return SecurityRuntimeDecisionCode.RECOVERY_REQUIRED
    }

    if (!allowsProtectedRuntime()) {
        return SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
    }

    return null
}

internal fun SecurityRuntimeAccessSnapshot.isVerified(): Boolean =
    containment.effectiveTrustState == TrustState.VERIFIED

internal fun SecurityRuntimeAccessSnapshot.isDegraded(): Boolean =
    containment.effectiveTrustState == TrustState.DEGRADED

internal fun SecurityRuntimeAccessSnapshot.isEmergencyLimited(): Boolean =
    containment.effectiveTrustState == TrustState.EMERGENCY_LIMITED

private fun SecurityRuntimeAccessSnapshot.isCompromised(): Boolean =
    containment.effectiveTrustState == TrustState.COMPROMISED

private fun SecurityRuntimeAccessSnapshot.requiresRecovery(): Boolean =
    containment.requireRecovery

private fun SecurityRuntimeAccessSnapshot.allowsProtectedRuntime(): Boolean =
    containment.capabilityEnvelope.allowProtectedRuntime

internal fun SecurityRuntimeAccessSnapshot.allowsSensitiveOperations(): Boolean =
    containment.capabilityEnvelope.allowSensitiveOperations