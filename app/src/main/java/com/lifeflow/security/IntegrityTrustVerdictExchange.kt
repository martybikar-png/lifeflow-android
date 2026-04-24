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

internal enum class IntegrityTrustAttestationStatus {
    CAPTURED,
    UNAVAILABLE
}

internal enum class IntegrityTrustAttestationFailureKind {
    UNAVAILABLE,
    HARD_FAILURE
}

internal enum class IntegrityTrustAttestationChainVerdict {
    VERIFIED,
    FAILED,
    UNEVALUATED
}

internal enum class IntegrityTrustAttestationChallengeVerdict {
    MATCHED,
    MISMATCHED,
    UNEVALUATED
}

internal enum class IntegrityTrustAttestationRootVerdict {
    GOOGLE_TRUSTED,
    UNTRUSTED,
    UNEVALUATED
}

internal enum class IntegrityTrustAttestationRevocationVerdict {
    CLEAN,
    REVOKED,
    UNCHECKED,
    UNEVALUATED
}

internal enum class IntegrityTrustAttestationAppBindingVerdict {
    MATCHED,
    MISMATCHED,
    UNEVALUATED
}

internal data class IntegrityTrustAttestationEvidence(
    val status: IntegrityTrustAttestationStatus,
    val keyAlias: String,
    val chainEntryCount: Int,
    val challengeBase64: String? = null,
    val certificateChainDerBase64: List<String> = emptyList(),
    val challengeSha256: String? = null,
    val leafCertificateSha256: String? = null,
    val strongBoxRequested: Boolean,
    val failureKind: IntegrityTrustAttestationFailureKind? = null,
    val failureReason: String? = null
) {
    init {
        require(keyAlias.isNotBlank()) { "keyAlias must not be blank." }
        require(chainEntryCount >= 0) { "chainEntryCount must be >= 0." }
        certificateChainDerBase64.forEach { entry ->
            require(entry.isNotBlank()) {
                "certificateChainDerBase64 entries must not be blank."
            }
        }
        challengeSha256?.let {
            require(it.isNotBlank()) { "challengeSha256 must not be blank when present." }
        }
        leafCertificateSha256?.let {
            require(it.isNotBlank()) { "leafCertificateSha256 must not be blank when present." }
        }
        failureReason?.let {
            require(it.isNotBlank()) { "failureReason must not be blank when present." }
        }

        when (status) {
            IntegrityTrustAttestationStatus.CAPTURED -> {
                require(!challengeBase64.isNullOrBlank()) {
                    "challengeBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.isNotEmpty()) {
                    "certificateChainDerBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.size == chainEntryCount) {
                    "chainEntryCount must match certificateChainDerBase64 size for CAPTURED attestation evidence."
                }
                require(!challengeSha256.isNullOrBlank()) {
                    "challengeSha256 is required for CAPTURED attestation evidence."
                }
                require(!leafCertificateSha256.isNullOrBlank()) {
                    "leafCertificateSha256 is required for CAPTURED attestation evidence."
                }
                require(failureKind == null) {
                    "failureKind must be null for CAPTURED attestation evidence."
                }
                require(failureReason == null) {
                    "failureReason must be null for CAPTURED attestation evidence."
                }
            }

            IntegrityTrustAttestationStatus.UNAVAILABLE -> {
                require(certificateChainDerBase64.isEmpty()) {
                    "certificateChainDerBase64 must be empty for UNAVAILABLE attestation evidence."
                }
                require(failureKind != null) {
                    "failureKind is required for UNAVAILABLE attestation evidence."
                }
                require(!failureReason.isNullOrBlank()) {
                    "failureReason is required for UNAVAILABLE attestation evidence."
                }
            }
        }
    }
}

internal data class IntegrityTrustAttestationVerification(
    val chainVerdict: IntegrityTrustAttestationChainVerdict,
    val challengeVerdict: IntegrityTrustAttestationChallengeVerdict,
    val rootVerdict: IntegrityTrustAttestationRootVerdict,
    val revocationVerdict: IntegrityTrustAttestationRevocationVerdict,
    val appBindingVerdict: IntegrityTrustAttestationAppBindingVerdict,
    val detail: String? = null
) {
    init {
        detail?.let {
            require(it.isNotBlank()) { "detail must not be blank when present." }
        }
    }
}

internal fun SecurityKeyAttestationEvidence.toIntegrityTrustAttestationEvidence():
    IntegrityTrustAttestationEvidence =
    IntegrityTrustAttestationEvidence(
        status = when (status) {
            SecurityKeyAttestationStatus.CAPTURED ->
                IntegrityTrustAttestationStatus.CAPTURED

            SecurityKeyAttestationStatus.UNAVAILABLE ->
                IntegrityTrustAttestationStatus.UNAVAILABLE
        },
        keyAlias = keyAlias,
        chainEntryCount = chainEntryCount,
        challengeBase64 = challengeBase64,
        certificateChainDerBase64 = certificateChainDerBase64,
        challengeSha256 = challengeSha256,
        leafCertificateSha256 = leafCertificateSha256,
        strongBoxRequested = strongBoxRequested,
        failureKind = failureKind?.let {
            when (it) {
                SecurityKeyAttestationFailureKind.UNAVAILABLE ->
                    IntegrityTrustAttestationFailureKind.UNAVAILABLE

                SecurityKeyAttestationFailureKind.HARD_FAILURE ->
                    IntegrityTrustAttestationFailureKind.HARD_FAILURE
            }
        },
        failureReason = failureReason
    )

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
