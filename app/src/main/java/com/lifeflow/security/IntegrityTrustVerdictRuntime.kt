package com.lifeflow.security

import com.lifeflow.security.integrity.PlayIntegrityVerifier

/**
 * Orchestrates the integrity trust verdict flow:
 * payload -> requestHash -> Play Integrity token -> external verdict authority.
 *
 * Purpose:
 * - keep request orchestration out of IntegrityTrustRuntime wiring
 * - make verdict logic directly unit-testable
 * - preserve fail-safe degraded responses for token acquisition problems
 */
internal class IntegrityTrustVerdictRuntime(
    private val cloudProjectNumber: Long,
    private val generateRequestHash: (String) -> String,
    private val requestIntegrityToken: suspend (
        String,
        Long
    ) -> PlayIntegrityVerifier.IntegrityResult,
    private val requestExternalVerdict: (IntegrityTrustVerdictRequest) -> IntegrityTrustVerdictResponse
) {

    suspend fun requestServerVerdict(
        payload: String
    ): IntegrityTrustVerdictResponse {
        require(payload.isNotBlank()) { "payload must not be blank." }

        val requestHash = generateRequestHash(payload)

        return when (
            val tokenResult = requestIntegrityToken(
                requestHash,
                cloudProjectNumber
            )
        ) {
            is PlayIntegrityVerifier.IntegrityResult.Success -> {
                requestExternalVerdict(
                    IntegrityTrustVerdictRequest(
                        requestHash = requestHash,
                        integrityToken = tokenResult.token
                    )
                )
            }

            is PlayIntegrityVerifier.IntegrityResult.Failure -> {
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = "PLAY_INTEGRITY_REQUEST_FAILED: ${tokenResult.error}"
                )
            }

            PlayIntegrityVerifier.IntegrityResult.NotConfigured -> {
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = "PLAY_INTEGRITY_NOT_CONFIGURED"
                )
            }
        }
    }
}
