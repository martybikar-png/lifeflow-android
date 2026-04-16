package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort

/**
 * Production adapter for the external immutable emergency audit sink.
 */
internal class ExternalEmergencyAuditSink(
    private val transport: EmergencyAuthorityTransport
) : EmergencyAuditSinkPort {

    override fun append(record: EmergencyAuditRecord): String {
        return transport.appendAuditRecord(record)
    }
}
