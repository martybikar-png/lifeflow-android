package com.lifeflow.domain.security

/**
 * Domain-level trust posture.
 *
 * This type belongs to the explicit boundary contract layer and must remain
 * independent from app/security implementation details.
 */
enum class TrustState {
    VERIFIED,
    DEGRADED,
    EMERGENCY_LIMITED,
    COMPROMISED;

    val isFailClosed: Boolean
        get() = this == COMPROMISED

    val permitsWrites: Boolean
        get() = this == VERIFIED

    val isEmergency: Boolean
        get() = this == EMERGENCY_LIMITED
}
