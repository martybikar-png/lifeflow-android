package com.lifeflow.security

internal enum class IntegrityTrustRpcVerdict {
    VERIFIED,
    DEGRADED,
    COMPROMISED
}

internal enum class IntegrityTrustRpcDecision {
    ALLOW,
    STEP_UP,
    DEGRADED,
    DENY,
    LOCK
}

internal enum class IntegrityTrustRpcVerdictSource {
    PLAY_INTEGRITY_STANDARD_SERVER
}

internal fun defaultIntegrityTrustRpcDecisionFor(
    verdict: IntegrityTrustRpcVerdict
): IntegrityTrustRpcDecision =
    when (verdict) {
        IntegrityTrustRpcVerdict.VERIFIED -> IntegrityTrustRpcDecision.ALLOW
        IntegrityTrustRpcVerdict.DEGRADED -> IntegrityTrustRpcDecision.DEGRADED
        IntegrityTrustRpcVerdict.COMPROMISED -> IntegrityTrustRpcDecision.LOCK
    }
