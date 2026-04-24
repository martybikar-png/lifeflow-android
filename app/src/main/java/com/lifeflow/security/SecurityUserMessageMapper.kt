package com.lifeflow.security

internal const val SECURITY_COMPROMISED_USER_MESSAGE =
    "Security compromised. Reset vault is required before continuing."
internal const val SECURITY_DEGRADED_USER_MESSAGE =
    "Security degraded. Please authenticate again."
internal const val SECURITY_EMERGENCY_LIMITED_USER_MESSAGE =
    "Emergency limited mode is active. Standard protected runtime remains blocked."
internal const val AUTH_REQUIRED_USER_MESSAGE =
    "Authentication required. Please authenticate again."
internal const val PROTECTED_RUNTIME_BLOCKED_USER_MESSAGE =
    "Protected runtime is blocked by current security policy."
internal const val RECOVERY_REQUIRED_USER_MESSAGE =
    "Recovery is required before protected access can continue."
internal const val ACCESS_LOCKED_USER_MESSAGE =
    "Access locked. Please authenticate again."

internal fun SecurityRuntimeAccessDecision.toStandardProtectedUserMessage(): String =
    when (denialCode) {
        SecurityRuntimeDecisionCode.COMPROMISED ->
            SECURITY_COMPROMISED_USER_MESSAGE

        SecurityRuntimeDecisionCode.RECOVERY_REQUIRED ->
            RECOVERY_REQUIRED_USER_MESSAGE

        SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED ->
            PROTECTED_RUNTIME_BLOCKED_USER_MESSAGE

        SecurityRuntimeDecisionCode.EMERGENCY_LIMITED ->
            SECURITY_EMERGENCY_LIMITED_USER_MESSAGE

        SecurityRuntimeDecisionCode.AUTH_REQUIRED,
        null ->
            AUTH_REQUIRED_USER_MESSAGE

        else ->
            AUTH_REQUIRED_USER_MESSAGE
    }

internal fun lockedReasonToUserMessage(reason: String): String =
    when (reason.toSecurityLockedReasonOrNull()) {
        SecurityLockedReason.COMPROMISED ->
            SECURITY_COMPROMISED_USER_MESSAGE

        SecurityLockedReason.RECOVERY_REQUIRED ->
            RECOVERY_REQUIRED_USER_MESSAGE

        SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED ->
            PROTECTED_RUNTIME_BLOCKED_USER_MESSAGE

        SecurityLockedReason.EMERGENCY_LIMITED ->
            SECURITY_EMERGENCY_LIMITED_USER_MESSAGE

        SecurityLockedReason.AUTH_REQUIRED ->
            AUTH_REQUIRED_USER_MESSAGE

        null ->
            if (reason.isBlank()) {
                ACCESS_LOCKED_USER_MESSAGE
            } else {
                reason
            }
    }
