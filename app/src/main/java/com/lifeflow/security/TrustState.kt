package com.lifeflow.security

/**
 * Phase V — Trust States
 *
 * Source-of-truth security posture states used by the rule engine
 * and auth/session boundary.
 */
enum class TrustState {

    /**
     * Normal trusted operation.
     */
    VERIFIED,

    /**
     * Elevated-risk mode.
     * System remains available only in a reduced, fail-closed posture.
     */
    DEGRADED,

    /**
     * Terminal lockdown state.
     * All protected operations must be denied.
     */
    COMPROMISED;

    val isFailClosed: Boolean
        get() = this == COMPROMISED
}