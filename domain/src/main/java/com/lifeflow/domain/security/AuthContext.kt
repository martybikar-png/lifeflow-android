package com.lifeflow.domain.security

/**
 * Minimal domain-safe authorization context.
 *
 * This context must stay free of Android/UI/security implementation details.
 * It describes only the facts relevant for domain authorization decisions.
 */
data class AuthContext(
    val hasRecentAuthentication: Boolean,
    val requiresStrictAuth: Boolean = false,
    val emergencyRequest: EmergencyAccessRequest? = null,
    val trustedBaseOnly: Boolean = false
)
