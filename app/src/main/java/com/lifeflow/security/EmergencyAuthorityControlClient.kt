package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import io.grpc.ManagedChannel

/**
 * Control-side client boundary for external emergency authority RPCs.
 *
 * Purpose:
 * - keep control RPC behavior separate from transport/channel wiring
 * - keep future generated stubs behind this boundary
 * - remain explicitly fail-closed until the real RPC contract exists
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
        throw SecurityException(
            unavailableRpcContractMessage(operationName = "registerIssuedArtifact")
        )
    }

    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        throw SecurityException(
            unavailableRpcContractMessage(operationName = "consumeIssuedArtifact")
        )
    }

    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        throw SecurityException(
            unavailableRpcContractMessage(operationName = "markArtifactExpiredUnused")
        )
    }

    private fun unavailableRpcContractMessage(operationName: String): String {
        return "gRPC emergency authority control RPC contract is not configured for " +
            "$operationName on ${channel.safeAuthorityLabel()}. Control channel remains fail-closed."
    }
}

private fun ManagedChannel.safeAuthorityLabel(): String {
    return runCatching { authority() }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "unavailable-authority"
}