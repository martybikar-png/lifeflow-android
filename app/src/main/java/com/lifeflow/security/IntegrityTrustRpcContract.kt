package com.lifeflow.security

/**
 * Backend/RPC-side contract for integrity trust verdict exchange.
 *
 * Purpose:
 * - isolate future generated gRPC/proto models from internal runtime models
 * - keep transport/client wiring independent from domain-facing verdict models
 */
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

internal enum class IntegrityTrustRpcAppRecognitionVerdict {
    PLAY_RECOGNIZED,
    UNRECOGNIZED_VERSION,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcDeviceRecognitionVerdict {
    MEETS_BASIC_INTEGRITY,
    MEETS_DEVICE_INTEGRITY,
    MEETS_STRONG_INTEGRITY,
    MEETS_VIRTUAL_INTEGRITY
}

internal enum class IntegrityTrustRpcAppLicensingVerdict {
    LICENSED,
    UNLICENSED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcPlayProtectVerdict {
    NO_ISSUES,
    NO_DATA,
    POSSIBLE_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcAttestationStatus {
    CAPTURED,
    UNAVAILABLE
}

internal enum class IntegrityTrustRpcAttestationFailureKind {
    UNAVAILABLE,
    HARD_FAILURE
}

internal enum class IntegrityTrustRpcAttestationChainVerdict {
    VERIFIED,
    FAILED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcAttestationChallengeVerdict {
    MATCHED,
    MISMATCHED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcAttestationRootVerdict {
    GOOGLE_TRUSTED,
    UNTRUSTED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcAttestationRevocationVerdict {
    CLEAN,
    REVOKED,
    UNCHECKED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcAttestationAppBindingVerdict {
    MATCHED,
    MISMATCHED,
    UNEVALUATED
}

internal data class IntegrityTrustRpcClaims(
    val appRecognitionVerdict: IntegrityTrustRpcAppRecognitionVerdict? = null,
    val deviceRecognitionVerdicts: Set<IntegrityTrustRpcDeviceRecognitionVerdict> = emptySet(),
    val appLicensingVerdict: IntegrityTrustRpcAppLicensingVerdict? = null,
    val playProtectVerdict: IntegrityTrustRpcPlayProtectVerdict? = null
)

internal data class IntegrityTrustRpcAttestationEvidence(
    val status: IntegrityTrustRpcAttestationStatus,
    val keyAlias: String,
    val chainEntryCount: Int,
    val challengeBase64: String? = null,
    val certificateChainDerBase64: List<String> = emptyList(),
    val challengeSha256: String? = null,
    val leafCertificateSha256: String? = null,
    val strongBoxRequested: Boolean,
    val failureKind: IntegrityTrustRpcAttestationFailureKind? = null,
    val failureReason: String? = null
) {
    init {
        require(keyAlias.isNotBlank()) { "RPC keyAlias must not be blank." }
        require(chainEntryCount >= 0) { "RPC chainEntryCount must be >= 0." }
        certificateChainDerBase64.forEach { entry ->
            require(entry.isNotBlank()) {
                "RPC certificateChainDerBase64 entries must not be blank."
            }
        }

        when (status) {
            IntegrityTrustRpcAttestationStatus.CAPTURED -> {
                require(!challengeBase64.isNullOrBlank()) {
                    "RPC challengeBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.isNotEmpty()) {
                    "RPC certificateChainDerBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.size == chainEntryCount) {
                    "RPC chainEntryCount must match certificateChainDerBase64 size for CAPTURED attestation evidence."
                }
                require(!challengeSha256.isNullOrBlank()) {
                    "RPC challengeSha256 is required for CAPTURED attestation evidence."
                }
                require(!leafCertificateSha256.isNullOrBlank()) {
                    "RPC leafCertificateSha256 is required for CAPTURED attestation evidence."
                }
                require(failureKind == null) {
                    "RPC failureKind must be null for CAPTURED attestation evidence."
                }
                require(failureReason == null) {
                    "RPC failureReason must be null for CAPTURED attestation evidence."
                }
            }

            IntegrityTrustRpcAttestationStatus.UNAVAILABLE -> {
                require(certificateChainDerBase64.isEmpty()) {
                    "RPC certificateChainDerBase64 must be empty for UNAVAILABLE attestation evidence."
                }
                require(failureKind != null) {
                    "RPC failureKind is required for UNAVAILABLE attestation evidence."
                }
                require(!failureReason.isNullOrBlank()) {
                    "RPC failureReason is required for UNAVAILABLE attestation evidence."
                }
            }
        }
    }
}

internal data class IntegrityTrustRpcAttestationVerification(
    val chainVerdict: IntegrityTrustRpcAttestationChainVerdict,
    val challengeVerdict: IntegrityTrustRpcAttestationChallengeVerdict,
    val rootVerdict: IntegrityTrustRpcAttestationRootVerdict,
    val revocationVerdict: IntegrityTrustRpcAttestationRevocationVerdict,
    val appBindingVerdict: IntegrityTrustRpcAttestationAppBindingVerdict,
    val detail: String? = null
) {
    init {
        detail?.let {
            require(it.isNotBlank()) { "RPC detail must not be blank when present." }
        }
    }
}

internal data class IntegrityTrustRpcRequest(
    val requestHash: String,
    val requestPayload: String,
    val integrityToken: String,
    val attestationEvidence: IntegrityTrustRpcAttestationEvidence? = null
) {
    init {
        require(requestHash.isNotBlank()) { "RPC requestHash must not be blank." }
        require(requestPayload.isNotBlank()) { "RPC requestPayload must not be blank." }
        require(integrityToken.isNotBlank()) { "RPC integrityToken must not be blank." }
    }
}

private fun defaultIntegrityTrustRpcDecisionFor(
    verdict: IntegrityTrustRpcVerdict
): IntegrityTrustRpcDecision =
    when (verdict) {
        IntegrityTrustRpcVerdict.VERIFIED -> IntegrityTrustRpcDecision.ALLOW
        IntegrityTrustRpcVerdict.DEGRADED -> IntegrityTrustRpcDecision.DEGRADED
        IntegrityTrustRpcVerdict.COMPROMISED -> IntegrityTrustRpcDecision.LOCK
    }

internal data class IntegrityTrustRpcResponse(
    val verdict: IntegrityTrustRpcVerdict,
    val reason: String,
    val requestHashEcho: String,
    val requestBindingVerified: Boolean = false,
    val serverTimestampEpochMs: Long,
    val policyVersion: String,
    val verdictSource: IntegrityTrustRpcVerdictSource,
    val claims: IntegrityTrustRpcClaims = IntegrityTrustRpcClaims(),
    val attestationVerification: IntegrityTrustRpcAttestationVerification? = null,
    val decision: IntegrityTrustRpcDecision = defaultIntegrityTrustRpcDecisionFor(verdict),
    val decisionReasonCode: String? = null
) {
    init {
        require(reason.isNotBlank()) { "RPC reason must not be blank." }
        require(requestHashEcho.isNotBlank()) { "RPC requestHashEcho must not be blank." }
        require(serverTimestampEpochMs > 0L) { "RPC serverTimestampEpochMs must be > 0." }
        require(policyVersion.isNotBlank()) { "RPC policyVersion must not be blank." }
        decisionReasonCode?.let {
            require(it.isNotBlank()) { "RPC decisionReasonCode must not be blank when present." }
        }
    }
}
