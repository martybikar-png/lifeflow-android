package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import io.grpc.ManagedChannel

/**
 * Audit-side client boundary for external emergency authority RPCs.
 *
 * Purpose:
 * - keep audit RPC behavior separate from transport/channel wiring
 * - keep future generated stubs behind this boundary
 * - remain explicitly fail-closed until the real RPC contract exists
 */
internal interface EmergencyAuthorityAuditClient {
    fun appendAuditRecord(record: EmergencyAuditRecord): String
}

internal class GrpcEmergencyAuthorityAuditClient(
    private val channel: ManagedChannel
) : EmergencyAuthorityAuditClient {

    override fun appendAuditRecord(record: EmergencyAuditRecord): String {
        throw SecurityException(
            unavailableRpcContractMessage(operationName = "appendAuditRecord")
        )
    }

    private fun unavailableRpcContractMessage(operationName: String): String {
        return "gRPC emergency authority audit RPC contract is not configured for " +
            "$operationName on ${channel.safeAuthorityLabel()}. Audit channel remains fail-closed."
    }
}

private fun ManagedChannel.safeAuthorityLabel(): String {
    return runCatching { authority() }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "unavailable-authority"
}