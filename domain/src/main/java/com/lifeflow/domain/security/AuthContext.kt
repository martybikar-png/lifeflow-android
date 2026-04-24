package com.lifeflow.domain.security

/**
 * Explicit authorization policy requested by the caller.
 *
 * This remains domain-safe and avoids leaking app/security implementation flags
 * through boolean combinations.
 */
enum class AuthorizationPolicy(
    val requiresVerifiedTrust: Boolean,
    val requiresTrustedBaseReadOnly: Boolean
) {
    STANDARD_PROTECTED(
        requiresVerifiedTrust = false,
        requiresTrustedBaseReadOnly = false
    ),
    STRICT_PROTECTED(
        requiresVerifiedTrust = true,
        requiresTrustedBaseReadOnly = false
    ),
    TRUSTED_BASE_READ_ONLY(
        requiresVerifiedTrust = false,
        requiresTrustedBaseReadOnly = true
    )
}

/**
 * Minimal domain-safe authorization context.
 *
 * This context must stay free of Android/UI/security implementation details.
 * It describes only the facts relevant for domain authorization decisions.
 */
data class AuthContext(
    val hasRecentAuthentication: Boolean,
    val authorizationPolicy: AuthorizationPolicy = AuthorizationPolicy.STANDARD_PROTECTED,
    val emergencyRequest: EmergencyAccessRequest? = null
)
