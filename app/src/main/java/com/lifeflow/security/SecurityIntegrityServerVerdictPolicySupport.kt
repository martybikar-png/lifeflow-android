package com.lifeflow.security

internal fun securityIntegrityServerDecisionCompatible(
    verdict: SecurityIntegrityTrustVerdict,
    decision: IntegrityTrustDecision
): Boolean {
    return when (verdict) {
        SecurityIntegrityTrustVerdict.VERIFIED ->
            decision == IntegrityTrustDecision.ALLOW ||
                decision == IntegrityTrustDecision.STEP_UP

        SecurityIntegrityTrustVerdict.DEGRADED ->
            decision == IntegrityTrustDecision.STEP_UP ||
                decision == IntegrityTrustDecision.DEGRADED ||
                decision == IntegrityTrustDecision.DENY

        SecurityIntegrityTrustVerdict.COMPROMISED ->
            decision == IntegrityTrustDecision.DENY ||
                decision == IntegrityTrustDecision.LOCK
    }
}

internal fun securityIntegrityServerVerdictMetadataFailClosed(
    detail: String
): IntegrityTrustVerdictResponse {
    return IntegrityTrustVerdictResponse(
        verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
        reason = "SERVER_VERDICT_METADATA_INVALID: $detail",
        verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
        decision = IntegrityTrustDecision.LOCK,
        decisionReasonCode = "SERVER_METADATA_INVALID"
    )
}
