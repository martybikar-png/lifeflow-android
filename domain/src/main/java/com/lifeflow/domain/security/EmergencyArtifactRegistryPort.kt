package com.lifeflow.domain.security

enum class EmergencyArtifactConsumptionStatus {
    CONSUMED,
    MISSING,
    EXPIRED_UNUSED,
    ALREADY_CONSUMED
}

/**
 * One-time registry for activation artifacts.
 *
 * This is the source of truth for issued/consumed/expired artifact ids.
 */
interface EmergencyArtifactRegistryPort {
    fun registerIssuedArtifact(artifact: EmergencyActivationArtifact)
    fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus
    fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    )
}
