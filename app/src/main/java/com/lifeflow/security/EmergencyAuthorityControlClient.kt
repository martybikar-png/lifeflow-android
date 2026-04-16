package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import io.grpc.ManagedChannel

/**
 * Control-side client boundary for external emergency authority RPCs.
 *
 * Purpose:
 * - keep control RPC behavior separate from transport/channel wiring
 * - make future generated stubs sit here, not inside transport
 * - remain fail-closed until the real RPC contract exists
 */
internal interface EmergencyAuthorityControlClient {
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

internal class GrpcEmergencyAuthorityControlClient(
    private val channel: ManagedChannel
) : EmergencyAuthorityControlClient {

    override fun registerIssuedArtifact(artifact: EmergencyActivationArtifact) {
        channel
        throw SecurityException(
            "gRPC emergency authority control RPC contract is not configured yet. Control channel remains fail-closed."
        )
    }

    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        channel
        throw SecurityException(
            "gRPC emergency authority control RPC contract is not configured yet. Artifact consumption remains fail-closed."
        )
    }

    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        channel
        throw SecurityException(
            "gRPC emergency authority control RPC contract is not configured yet. Expiration propagation remains fail-closed."
        )
    }
}
