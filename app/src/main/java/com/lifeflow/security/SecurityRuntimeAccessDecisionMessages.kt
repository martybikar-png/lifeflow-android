package com.lifeflow.security

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