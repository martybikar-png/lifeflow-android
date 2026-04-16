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
     * Temporary emergency-limited posture.
     *
     * This state exists explicitly so the system can represent
     * break-glass runtime separately from DEGRADED.
     *
     * Until the dedicated emergency window + trusted-base allowlist
     * are implemented, this state remains fail-closed for standard
     * protected runtime operations.
     */
    EMERGENCY_LIMITED,

    /**
     * Terminal lockdown state.
     * All protected operations must be denied.
     */
    COMPROMISED;

    val isFailClosed: Boolean
        get() = this == COMPROMISED
}
