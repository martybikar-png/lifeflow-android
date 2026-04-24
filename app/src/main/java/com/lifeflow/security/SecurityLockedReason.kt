package com.lifeflow.security

internal enum class SecurityLockedReason {
    AUTH_REQUIRED,
    EMERGENCY_LIMITED,
    COMPROMISED,
    RECOVERY_REQUIRED,
    PROTECTED_RUNTIME_BLOCKED
}

internal fun SecurityLockedReason.withDetail(detail: String): String =
    "$name: $detail"

internal fun String.toSecurityLockedReasonOrNull(): SecurityLockedReason? {
    val prefix = substringBefore(':').trim()
    return SecurityLockedReason.entries.firstOrNull { it.name == prefix }
}
