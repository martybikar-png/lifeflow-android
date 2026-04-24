package com.lifeflow.security

import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import com.lifeflow.security.integrity.PlayIntegrityVerifier

/**
 * Orchestrates the integrity trust verdict flow:
 * payload -> requestHash -> request-bound key attestation evidence
 * + Play Integrity token -> external verdict authority.
 *
 * Purpose:
 * - keep request orchestration out of IntegrityTrustRuntime wiring
 * - make verdict logic directly unit-testable
 * - preserve fail-safe degraded responses for token acquisition problems
 * - preserve clearer semantics for transient vs unavailable vs hard failures
 * - preserve attestation failure context inside locally generated degraded reasons
 */
internal class IntegrityTrustVerdictRuntime(
    private val cloudProjectNumber: Long,
    private val generateRequestHash: (String) -> String,
    private val requestIntegrityToken: suspend (
        String,
        Long
    ) -> PlayIntegrityVerifier.IntegrityResult,
    private val captureRequestBoundKeyAttestationEvidence: (String) -> SecurityKeyAttestationEvidence,
    private val requestExternalVerdict: (IntegrityTrustVerdictRequest) -> IntegrityTrustVerdictResponse
) {

    suspend fun requestServerVerdict(
        payload: String
    ): IntegrityTrustVerdictResponse {
        require(payload.isNotBlank()) { "payload must not be blank." }

        val requestHash = generateRequestHash(payload)
        val rawAttestationEvidence = captureRequestBoundKeyAttestationEvidence(requestHash)
        val attestationEvidence = rawAttestationEvidence.toIntegrityTrustAttestationEvidence()
        val attestationFailureKind = rawAttestationEvidence.failureKind
        val attestationFailureReason = rawAttestationEvidence.failureReason

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
                        requestPayload = payload,
                        integrityToken = tokenResult.token,
                        attestationEvidence = attestationEvidence
                    )
                )
            }

            is PlayIntegrityVerifier.IntegrityResult.Failure -> {
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = appendAttestationReason(
                        primaryReason = integrityFailureReason(tokenResult),
                        attestationFailureKind = attestationFailureKind,
                        attestationFailureReason = attestationFailureReason
                    )
                )
            }

            PlayIntegrityVerifier.IntegrityResult.NotConfigured -> {
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = appendAttestationReason(
                        primaryReason = "PLAY_INTEGRITY_NOT_CONFIGURED",
                        attestationFailureKind = attestationFailureKind,
                        attestationFailureReason = attestationFailureReason
                    )
                )
            }
        }
    }

    private fun integrityFailureReason(
        tokenResult: PlayIntegrityVerifier.IntegrityResult.Failure
    ): String {
        val bucket = when (tokenResult.errorCode) {
            StandardIntegrityErrorCode.CLIENT_TRANSIENT_ERROR,
            STANDARD_INTEGRITY_INITIALIZATION_FAILED_CODE -> {
                "PLAY_INTEGRITY_TRANSIENT_FAILURE"
            }

            StandardIntegrityErrorCode.API_NOT_AVAILABLE -> {
                "PLAY_INTEGRITY_UNAVAILABLE"
            }

            StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID -> {
                "PLAY_INTEGRITY_PROVIDER_INVALID"
            }

            else -> {
                "PLAY_INTEGRITY_HARD_FAILURE"
            }
        }

        val codeSuffix = tokenResult.errorCode?.let { "[$it]" } ?: ""
        return "$bucket$codeSuffix: ${tokenResult.error}"
    }

    private fun appendAttestationReason(
        primaryReason: String,
        attestationFailureKind: SecurityKeyAttestationFailureKind?,
        attestationFailureReason: String?
    ): String {
        val normalizedFailureKind = attestationFailureKind?.name?.trim().orEmpty()
        val normalizedFailureReason = attestationFailureReason?.trim().orEmpty()

        if (normalizedFailureKind.isBlank() && normalizedFailureReason.isBlank()) {
            return primaryReason
        }

        return buildString {
            append(primaryReason)
            if (normalizedFailureKind.isNotBlank()) {
                append(" | ATTESTATION_KIND=")
                append(normalizedFailureKind)
            }
            if (normalizedFailureReason.isNotBlank()) {
                append(" | ATTESTATION_REASON=")
                append(normalizedFailureReason)
            }
        }
    }

    private companion object {
        /**
         * Google Play Integrity docs list Standard Integrity initialization failure as error code -102.
         * We keep the numeric mapping locally because the symbol is not available in the current compile surface.
         */
        private const val STANDARD_INTEGRITY_INITIALIZATION_FAILED_CODE = -102
    }
}
