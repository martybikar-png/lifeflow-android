package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import io.grpc.ManagedChannel

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
    val label = try {
        authority()
    } catch (_: Exception) {
        null
    }

    return label
        ?.takeIf { it.isNotBlank() }
        ?: "unavailable-authority"
}
