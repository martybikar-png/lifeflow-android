package com.lifeflow.domain.security

/**
 * Hard rules for sender-constrained emergency activation artifacts.
 *
 * Core principle:
 * - one activation artifact -> one dedicated key binding
 * - extension without rekey is forbidden
 */
object EmergencyKeyRotationPolicy {

    const val MAX_ACTIVATION_ARTIFACT_LIFETIME_MS: Long = 60_000L

    fun assertArtifactLifetimeAllowed(
        lifetimeMs: Long
    ) {
        require(lifetimeMs in 1..MAX_ACTIVATION_ARTIFACT_LIFETIME_MS) {
            "Activation artifact lifetime exceeds allowed policy."
        }
    }

    fun requiresFreshBinding(
        previousBinding: EmergencyActivationKeyBinding?,
        nextBinding: EmergencyActivationKeyBinding
    ): Boolean {
        if (previousBinding == null) {
            return true
        }

        return previousBinding.keyId != nextBinding.keyId ||
                previousBinding.confirmationKeyThumbprint != nextBinding.confirmationKeyThumbprint
    }

    fun assertExtensionUsesFreshBinding(
        previousArtifact: EmergencyActivationArtifact,
        nextBinding: EmergencyActivationKeyBinding
    ) {
        require(
            requiresFreshBinding(
                previousBinding = previousArtifact.keyBinding,
                nextBinding = nextBinding
            )
        ) {
            "Break-glass extension without rekey is forbidden."
        }
    }
}
