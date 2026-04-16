package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort

/**
 * Production adapter for the external one-time activation artifact registry.
 */
internal class ExternalEmergencyArtifactRegistry(
    private val transport: EmergencyAuthorityTransport
) : EmergencyArtifactRegistryPort {

    override fun registerIssuedArtifact(artifact: EmergencyActivationArtifact) {
        transport.registerIssuedArtifact(artifact)
    }

    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        return transport.consumeIssuedArtifact(
            artifactId = artifactId,
            consumedAtEpochMs = consumedAtEpochMs
        )
    }

    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        transport.markArtifactExpiredUnused(
            artifactId = artifactId,
            reason = reason,
            expiredAtEpochMs = expiredAtEpochMs
        )
    }
}
