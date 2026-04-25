package com.lifeflow.security

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
