package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Instrumentation-only emergency audit sink.
 *
 * Production rule:
 * - never used as a local fallback
 * - production uses the external immutable authority boundary and fails closed when unavailable
 *
 * Test rule:
 * - used only when EmergencyAuthorityBoundaryBootstrap starts with isInstrumentation = true
 */
object InstrumentationEmergencyAuditSink : EmergencyAuditSinkPort {

    private const val MAX_RECORDS = 500

    data class StoredRecord(
        val externalRecordId: String,
        val record: EmergencyAuditRecord
    )

    private val records = ConcurrentLinkedQueue<StoredRecord>()

    override fun append(record: EmergencyAuditRecord): String {
        val externalRecordId = UUID.randomUUID().toString()

        records.add(
            StoredRecord(
                externalRecordId = externalRecordId,
                record = record
            )
        )

        while (records.size > MAX_RECORDS) {
            records.poll()
        }

        return externalRecordId
    }

    fun getRecords(): List<StoredRecord> = records.toList()

    fun clear() {
        records.clear()
    }
}
