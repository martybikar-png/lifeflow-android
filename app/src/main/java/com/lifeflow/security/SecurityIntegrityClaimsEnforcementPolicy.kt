package com.lifeflow.security

/**
 * Claim-based enforcement policy for already metadata-validated server verdicts.
 *
 * Purpose:
 * - keep structured claim decisions out of transport mapping
 * - decide whether VERIFIED may remain VERIFIED
 * - degrade or compromise when claims indicate weaker trust than the raw server verdict
 */
internal class SecurityIntegrityClaimsEnforcementPolicy {

    fun enforce(
        response: IntegrityTrustVerdictResponse
    ): IntegrityTrustVerdictResponse {
        if (response.verdictSource != IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER) {
            return response
        }

        val decision = evaluateSecurityIntegrityClaims(response)

        return when (decision.target) {
            SecurityIntegrityClaimsTargetVerdict.KEEP -> response

            SecurityIntegrityClaimsTargetVerdict.DEGRADE -> response.copy(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = buildString {
                    append(response.reason)
                    append(" | claimsPolicy=DEGRADED")
                    decision.detail?.let {
                        append(": ")
                        append(it)
                    }
                }
            )

            SecurityIntegrityClaimsTargetVerdict.COMPROMISE -> response.copy(
                verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                reason = buildString {
                    append(response.reason)
                    append(" | claimsPolicy=COMPROMISED")
                    decision.detail?.let {
                        append(": ")
                        append(it)
                    }
                }
            )
        }
    }
}
