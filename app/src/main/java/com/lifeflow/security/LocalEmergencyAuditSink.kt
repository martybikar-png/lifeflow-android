package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Temporary local implementation of the external emergency audit sink.
 *
 * IMPORTANT:
 * - This is only the boundary implementation for now.
 * - It is not the final off-device immutable authority.
 */
object LocalEmergencyAuditSink : EmergencyAuditSinkPort {

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
