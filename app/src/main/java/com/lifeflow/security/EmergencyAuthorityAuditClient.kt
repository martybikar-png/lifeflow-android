package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import io.grpc.ManagedChannel

/**
 * Audit-side client boundary for external emergency authority RPCs.
 *
 * Purpose:
 * - keep audit RPC behavior separate from transport/channel wiring
 * - make future generated stubs sit here, not inside transport
 * - remain fail-closed until the real RPC contract exists
 */
internal interface EmergencyAuthorityAuditClient {
    fun appendAuditRecord(record: EmergencyAuditRecord): String
}

internal class GrpcEmergencyAuthorityAuditClient(
    private val channel: ManagedChannel
) : EmergencyAuthorityAuditClient {

    override fun appendAuditRecord(record: EmergencyAuditRecord): String {
        channel
        throw SecurityException(
            "gRPC emergency authority audit RPC contract is not configured yet. Audit channel remains fail-closed."
        )
    }
}
