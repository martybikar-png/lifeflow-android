package com.lifeflow.domain.security

/**
 * External immutable audit sink for break-glass lifecycle records.
 *
 * Contract:
 * - append must fail loudly on sink failure
 * - caller may fail-closed if append cannot be persisted
 * - returned value is the external sink record id / acknowledgement id
 */
interface EmergencyAuditSinkPort {
    fun append(record: EmergencyAuditRecord): String
}
