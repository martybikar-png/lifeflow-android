package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort

internal fun appendSecurityEmergencyApprovalSessionCreatedAudit(
    auditSink: EmergencyAuditSinkPort,
    session: EmergencyApprovalSession,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.APPROVAL_SESSION_CREATED,
            approvalSessionId = session.sessionId,
            requestHash = session.requestHash,
            reason = session.reason,
            trustedBaseOnly = session.trustedBaseOnly,
            detail = "Break-glass approval session created.",
            metadata = mapOf(
                "approvedWindowDurationMs" to session.approvedWindowDurationMs.toString(),
                "degradedCauseSnapshotId" to session.degradedCauseSnapshotId,
                "firstApproverId" to session.firstApproverId,
                "secondApproverId" to session.secondApproverId
            )
        )
    )
}

internal fun appendSecurityEmergencyActivationArtifactIssuedAudit(
    auditSink: EmergencyAuditSinkPort,
    artifact: EmergencyActivationArtifact,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.ACTIVATION_ARTIFACT_ISSUED,
            approvalSessionId = artifact.approvalSessionId,
            artifactId = artifact.artifactId,
            requestHash = artifact.requestHash,
            reason = artifact.reason,
            trustedBaseOnly = artifact.trustedBaseOnly,
            detail = "Break-glass activation artifact issued.",
            metadata = mapOf(
                "audience" to artifact.audience,
                "expiresAtEpochMs" to artifact.expiresAtEpochMs.toString(),
                "keyId" to artifact.keyBinding.keyId,
                "keyThumbprint" to artifact.keyBinding.confirmationKeyThumbprint
            )
        )
    )
}
