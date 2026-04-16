package com.lifeflow.domain.security

/**
 * Sender-constrained key binding for one activation artifact.
 *
 * This model describes the PoP / cnf binding expected by the verifier.
 * It stays domain-safe and does not assume a specific crypto provider.
 */
data class EmergencyActivationKeyBinding(
    val keyId: String,
    val confirmationKeyThumbprint: String,
    val algorithm: String,
    val createdAtEpochMs: Long,
    val hardwareBacked: Boolean,
    val exportable: Boolean
) {
    init {
        require(keyId.isNotBlank()) {
            "keyId must not be blank."
        }
        require(confirmationKeyThumbprint.isNotBlank()) {
            "confirmationKeyThumbprint must not be blank."
        }
        require(algorithm.isNotBlank()) {
            "algorithm must not be blank."
        }
        require(createdAtEpochMs >= 0L) {
            "createdAtEpochMs must be >= 0."
        }
    }

    val isProtectedHolderBinding: Boolean
        get() = hardwareBacked && !exportable
}
