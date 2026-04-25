package com.lifeflow.security

internal enum class IntegrityTrustVerdictSource {
    PLAY_INTEGRITY_STANDARD_SERVER,
    CLIENT_FAILSAFE
}

internal enum class IntegrityTrustDecision {
    ALLOW,
    STEP_UP,
    DEGRADED,
    DENY,
    LOCK
}

internal fun defaultIntegrityTrustDecisionFor(
    verdict: SecurityIntegrityTrustVerdict
): IntegrityTrustDecision =
    when (verdict) {
        SecurityIntegrityTrustVerdict.VERIFIED -> IntegrityTrustDecision.ALLOW
        SecurityIntegrityTrustVerdict.DEGRADED -> IntegrityTrustDecision.DEGRADED
        SecurityIntegrityTrustVerdict.COMPROMISED -> IntegrityTrustDecision.LOCK
    }

internal data class IntegrityTrustVerdictRequest(
    val requestHash: String,
    val requestPayload: String,
    val integrityToken: String,
    val attestationEvidence: IntegrityTrustAttestationEvidence? = null
) {
    init {
        require(requestHash.isNotBlank()) { "requestHash must not be blank." }
        require(requestPayload.isNotBlank()) { "requestPayload must not be blank." }
        require(integrityToken.isNotBlank()) { "integrityToken must not be blank." }
    }
}

internal data class IntegrityTrustVerdictResponse(
    val verdict: SecurityIntegrityTrustVerdict,
    val reason: String,
    val requestHashEcho: String? = null,
    val requestBindingVerified: Boolean? = null,
    val serverTimestampEpochMs: Long? = null,
    val policyVersion: String? = null,
    val verdictSource: IntegrityTrustVerdictSource =
        IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
    val claims: SecurityIntegrityVerdictClaims = SecurityIntegrityVerdictClaims(),
    val attestationVerification: IntegrityTrustAttestationVerification? = null,
    val decision: IntegrityTrustDecision = defaultIntegrityTrustDecisionFor(verdict),
    val decisionReasonCode: String? = null
) {
    init {
        require(reason.isNotBlank()) { "reason must not be blank." }
        requestHashEcho?.let {
            require(it.isNotBlank()) { "requestHashEcho must not be blank when present." }
        }
        serverTimestampEpochMs?.let {
            require(it > 0L) { "serverTimestampEpochMs must be > 0 when present." }
        }
        policyVersion?.let {
            require(it.isNotBlank()) { "policyVersion must not be blank when present." }
        }
        decisionReasonCode?.let {
            require(it.isNotBlank()) { "decisionReasonCode must not be blank when present." }
        }

        if (verdictSource == IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER) {
            require(!requestHashEcho.isNullOrBlank()) {
                "requestHashEcho is required for PLAY_INTEGRITY_STANDARD_SERVER."
            }
            require(serverTimestampEpochMs != null && serverTimestampEpochMs > 0L) {
                "serverTimestampEpochMs is required for PLAY_INTEGRITY_STANDARD_SERVER."
            }
            require(!policyVersion.isNullOrBlank()) {
                "policyVersion is required for PLAY_INTEGRITY_STANDARD_SERVER."
            }
        }
    }
}