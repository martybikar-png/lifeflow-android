package com.lifeflow.domain.security

/**
 * Explicit domain-level reasons why the system is considered compromised.
 *
 * This type belongs to the boundary contract layer and must remain free of
 * Android, keystore, biometric, or infrastructure-specific details.
 */
enum class CompromiseReason {
    CRYPTO_FAILURE,
    INTEGRITY_FAILURE,
    POLICY_VIOLATION,
    UNKNOWN_SECURITY_FAILURE
}
