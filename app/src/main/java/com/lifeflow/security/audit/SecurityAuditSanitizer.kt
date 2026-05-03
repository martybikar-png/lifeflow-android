package com.lifeflow.security.audit

internal fun sanitizeSecurityAuditValue(
    value: String
): String {
    if (value.isBlank()) return value

    var sanitized = value
    SECURITY_AUDIT_SENSITIVE_PATTERNS.forEach { pattern ->
        sanitized = sanitized.replace(pattern, "[REDACTED]")
    }

    return sanitized
}

private val SECURITY_AUDIT_SENSITIVE_PATTERNS = listOf(
    Regex("(?i)(password|passwd|pwd)\\s*[:=]\\s*\\S+"),
    Regex("(?i)(token|key|secret|auth)\\s*[:=]\\s*\\S+"),
    Regex("[A-Za-z0-9+/]{32,}={0,2}"),
    Regex("[0-9a-fA-F]{32,}")
)
