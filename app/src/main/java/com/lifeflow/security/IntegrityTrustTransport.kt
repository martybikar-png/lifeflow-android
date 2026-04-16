package com.lifeflow.security

/**
 * Transport boundary for external integrity trust verdict verification.
 *
 * Production rules:
 * - no local trust fallback
 * - no local self-verification shortcut
 * - fail closed when the external verdict authority is unavailable
 */
internal interface IntegrityTrustTransport : AutoCloseable {
    fun requestVerdict(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustVerdictResponse

    override fun close()
}

/**
 * Explicit fail-closed production placeholder until the real integrity trust
 * verdict transport is connected.
 */
internal object UnconfiguredIntegrityTrustTransport : IntegrityTrustTransport {

    private const val MESSAGE =
        "External integrity trust transport is not configured. Integrity trust verification remains fail-closed."

    override fun requestVerdict(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustVerdictResponse {
        throw SecurityException(MESSAGE)
    }

    override fun close() = Unit
}
