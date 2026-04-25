package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal fun consumeSecurityEmergencyActivationArtifactOrThrow(
    state: SecurityEmergencyAccessAuthorityState,
    currentArtifact: EmergencyActivationArtifact,
    artifact: EmergencyActivationArtifact,
    consumedAtEpochMs: Long
) {
    when (
        state.artifactRegistry().consumeIssuedArtifact(
            artifactId = artifact.artifactId,
            consumedAtEpochMs = consumedAtEpochMs
        )
    ) {
        EmergencyArtifactConsumptionStatus.MISSING -> {
            SecurityAuditLog.warning(
                EventType.BREAK_GLASS_REJECTED,
                "Break-glass activation artifact is missing from registry.",
                metadata = mapOf(
                    "artifactId" to artifact.artifactId
                )
            )
            throw SecurityException("Activation artifact is missing from registry.")
        }

        EmergencyArtifactConsumptionStatus.ALREADY_CONSUMED -> {
            SecurityAuditLog.warning(
                EventType.BREAK_GLASS_REJECTED,
                "Break-glass activation artifact was already consumed.",
                metadata = mapOf(
                    "artifactId" to artifact.artifactId
                )
            )
            throw SecurityException("Activation artifact was already consumed.")
        }

        EmergencyArtifactConsumptionStatus.EXPIRED_UNUSED -> {
            state.issuedActivationArtifact.compareAndSet(currentArtifact, null)

            val externalRecordId = appendSecurityEmergencyActivationArtifactExpiredUnusedAudit(
                auditSink = state.auditSink(),
                artifact = artifact,
                timestampEpochMs = consumedAtEpochMs
            )

            SecurityAuditLog.warning(
                EventType.BREAK_GLASS_EXPIRED,
                "Break-glass activation artifact expired before use.",
                metadata = mapOf(
                    "artifactId" to artifact.artifactId,
                    "approvalSessionId" to artifact.approvalSessionId,
                    "reason" to artifact.reason.name,
                    "expiresAtEpochMs" to artifact.expiresAtEpochMs.toString(),
                    "externalRecordId" to externalRecordId
                )
            )

            throw SecurityException("Activation artifact expired before use.")
        }

        EmergencyArtifactConsumptionStatus.CONSUMED -> Unit
    }
}

private fun appendSecurityEmergencyActivationArtifactExpiredUnusedAudit(
    auditSink: EmergencyAuditSinkPort,
    artifact: EmergencyActivationArtifact,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.ACTIVATION_ARTIFACT_EXPIRED_UNUSED,
            approvalSessionId = artifact.approvalSessionId,
            artifactId = artifact.artifactId,
            requestHash = artifact.requestHash,
            reason = artifact.reason,
            trustedBaseOnly = artifact.trustedBaseOnly,
            detail = "Break-glass activation artifact expired unused.",
            metadata = mapOf(
                "expiresAtEpochMs" to artifact.expiresAtEpochMs.toString()
            )
        )
    )
}
