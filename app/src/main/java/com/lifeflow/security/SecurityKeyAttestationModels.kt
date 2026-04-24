package com.lifeflow.security

internal enum class SecurityKeyAttestationStatus {
    CAPTURED,
    UNAVAILABLE
}

internal enum class SecurityKeyAttestationFailureKind {
    UNAVAILABLE,
    HARD_FAILURE
}

internal data class SecurityKeyAttestationEvidence(
    val status: SecurityKeyAttestationStatus,
    val keyAlias: String,
    val chainEntryCount: Int,
    val challengeBase64: String? = null,
    val certificateChainDerBase64: List<String> = emptyList(),
    val challengeSha256: String? = null,
    val leafCertificateSha256: String? = null,
    val strongBoxRequested: Boolean,
    val failureKind: SecurityKeyAttestationFailureKind? = null,
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

        when (status) {
            SecurityKeyAttestationStatus.CAPTURED -> validateCaptured()
            SecurityKeyAttestationStatus.UNAVAILABLE -> validateUnavailable()
        }
    }

    private fun validateCaptured() {
        require(chainEntryCount > 0) {
            "chainEntryCount must be > 0 for CAPTURED attestation evidence."
        }
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

    private fun validateUnavailable() {
        require(chainEntryCount == 0) {
            "chainEntryCount must be 0 for UNAVAILABLE attestation evidence."
        }
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