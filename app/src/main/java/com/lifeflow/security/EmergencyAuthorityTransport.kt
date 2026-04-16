package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyAuditRecord

/**
 * Transport boundary for the external emergency authority.
 *
 * Production rules:
 * - no local persistence fallback
 * - no best-effort buffering
 * - fail closed when the external authority is unavailable
 *
 * Target direction:
 * - external immutable audit sink
 * - external one-time artifact registry
 * - sender-constrained proof verified by cnf / confirmation binding
 */
internal interface EmergencyAuthorityTransport {
    fun appendAuditRecord(record: EmergencyAuditRecord): String

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

/**
 * Explicit fail-closed production placeholder until the real external authority
 * transport is connected.
 *
 * This is intentionally not a fallback and not a local substitute.
 */
internal object UnconfiguredEmergencyAuthorityTransport : EmergencyAuthorityTransport {

    private const val MESSAGE =
        "External emergency authority transport is not configured. Break-glass remains fail-closed."

    override fun appendAuditRecord(record: EmergencyAuditRecord): String {
        throw SecurityException(MESSAGE)
    }

    override fun registerIssuedArtifact(artifact: EmergencyActivationArtifact) {
        throw SecurityException(MESSAGE)
    }

    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        throw SecurityException(MESSAGE)
    }

    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        throw SecurityException(MESSAGE)
    }
}
