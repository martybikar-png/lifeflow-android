package com.lifeflow.security

/**
 * Phase V — Trust States
 *
 * VERIFIED:
 * - normal operation
 *
 * DEGRADED:
 * - partial operation (typically read-only) due to elevated risk conditions
 *
 * COMPROMISED:
 * - fail-closed, deny everything
 */
enum class TrustState {
    VERIFIED,
    DEGRADED,
    COMPROMISED
}