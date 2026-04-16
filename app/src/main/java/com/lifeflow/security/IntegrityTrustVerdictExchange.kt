package com.lifeflow.security

internal enum class IntegrityTrustVerdictSource {
    PLAY_INTEGRITY_STANDARD_SERVER,
    CLIENT_FAILSAFE
}

internal data class IntegrityTrustVerdictRequest(
    val requestHash: String,
    val integrityToken: String
) {
    init {
        require(requestHash.isNotBlank()) { "requestHash must not be blank." }
        require(integrityToken.isNotBlank()) { "integrityToken must not be blank." }
    }
}

internal data class IntegrityTrustVerdictResponse(
    val verdict: SecurityIntegrityTrustVerdict,
    val reason: String,
    val requestHashEcho: String? = null,
    val serverTimestampEpochMs: Long? = null,
    val policyVersion: String? = null,
    val verdictSource: IntegrityTrustVerdictSource =
        IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
    val claims: SecurityIntegrityVerdictClaims = SecurityIntegrityVerdictClaims()
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
