package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort

internal fun appendSecurityEmergencyWindowActivatedAudit(
    auditSink: EmergencyAuditSinkPort,
    artifact: EmergencyActivationArtifact,
    window: EmergencyAccessWindow,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.ACTIVATION_ARTIFACT_CONSUMED,
            approvalSessionId = artifact.approvalSessionId,
            artifactId = artifact.artifactId,
            windowId = window.windowId,
            requestHash = artifact.requestHash,
            reason = artifact.reason,
            trustedBaseOnly = artifact.trustedBaseOnly,
            detail = "Break-glass emergency window activated.",
            metadata = mapOf(
                "startedAtEpochMs" to window.startedAtEpochMs.toString(),
                "expiresAtEpochMs" to window.expiresAtEpochMs.toString()
            )
        )
    )
}
