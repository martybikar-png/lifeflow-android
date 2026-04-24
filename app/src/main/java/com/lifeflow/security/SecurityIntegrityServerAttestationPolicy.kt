package com.lifeflow.security

/**
 * Enforces server-side attestation verification before claims are trusted.
 */
internal class SecurityIntegrityServerAttestationPolicy {
    fun enforce(
        response: IntegrityTrustVerdictResponse
    ): IntegrityTrustVerdictResponse {
        val verification = response.attestationVerification
            ?: return attestationFailClosed(
                detail = "missing attestationVerification",
                verification = null
            )

        if (verification.challengeVerdict !=
            IntegrityTrustAttestationChallengeVerdict.MATCHED
        ) {
            return attestationFailClosed(
                detail = "challenge verdict is ${verification.challengeVerdict}",
                verification = verification
            )
        }

        if (verification.chainVerdict !=
            IntegrityTrustAttestationChainVerdict.VERIFIED
        ) {
            return attestationFailClosed(
                detail = "chain verdict is ${verification.chainVerdict}",
                verification = verification
            )
        }

        if (verification.rootVerdict !=
            IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED
        ) {
            return attestationFailClosed(
                detail = "root verdict is ${verification.rootVerdict}",
                verification = verification
            )
        }

        if (verification.revocationVerdict !=
            IntegrityTrustAttestationRevocationVerdict.CLEAN
        ) {
            return attestationFailClosed(
                detail = "revocation verdict is ${verification.revocationVerdict}",
                verification = verification
            )
        }

        if (verification.appBindingVerdict !=
            IntegrityTrustAttestationAppBindingVerdict.MATCHED
        ) {
            return attestationFailClosed(
                detail = "app binding verdict is ${verification.appBindingVerdict}",
                verification = verification
            )
        }

        return response.copy(
            reason = buildString {
                append(response.reason)
                append(" | attestationChain=")
                append(verification.chainVerdict)
                append(" | attestationChallenge=")
                append(verification.challengeVerdict)
                append(" | attestationRoot=")
                append(verification.rootVerdict)
                append(" | attestationRevocation=")
                append(verification.revocationVerdict)
                append(" | attestationAppBinding=")
                append(verification.appBindingVerdict)
                verification.detail?.let {
                    append(" | attestationDetail=")
                    append(it)
                }
            }
        )
    }

    private fun attestationFailClosed(
        detail: String,
        verification: IntegrityTrustAttestationVerification?
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "SERVER_VERDICT_ATTESTATION_INVALID: $detail",
            verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            attestationVerification = verification,
            decision = IntegrityTrustDecision.LOCK,
            decisionReasonCode = "SERVER_ATTESTATION_INVALID"
        )
    }
}