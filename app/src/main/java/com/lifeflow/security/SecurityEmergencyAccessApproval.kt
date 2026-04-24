package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import com.lifeflow.domain.security.EmergencyKeyRotationPolicy
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.UUID

internal fun createSecurityEmergencyApprovalSession(
    state: SecurityEmergencyAccessAuthorityState,
    request: EmergencyAccessRequest,
    degradedCauseSnapshotId: String,
    firstApproverId: String,
    secondApproverId: String
): EmergencyApprovalSession {
    requireApprovalSessionTrustState(request)

    val boundedDurationMs = request.requestedDurationMs.coerceAtMost(
        SecurityEmergencyAccessMaxWindowMs
    )
    val now = System.currentTimeMillis()

    val requestHash = sha256Hex(
        buildString {
            append(request.reason.name)
            append('|')
            append(request.requestedAtEpochMs)
            append('|')
            append(boundedDurationMs)
            append('|')
            append("trustedBaseOnly=true")
            append('|')
            append(degradedCauseSnapshotId)
        }
    )

    val session = EmergencyApprovalSession(
        sessionId = UUID.randomUUID().toString(),
        requestHash = requestHash,
        reason = request.reason,
        requestedAtEpochMs = request.requestedAtEpochMs,
        approvedAtEpochMs = now,
        approvedWindowDurationMs = boundedDurationMs,
        trustedBaseOnly = true,
        degradedCauseSnapshotId = degradedCauseSnapshotId,
        firstApproverId = firstApproverId,
        secondApproverId = secondApproverId
    )

    val externalRecordId = appendApprovalSessionCreatedAudit(
        auditSink = state.auditSink(),
        session = session,
        timestampEpochMs = now
    )

    state.activeApprovalSession.set(session)
    state.issuedActivationArtifact.set(null)

    SecurityAuditLog.info(
        EventType.RECOVERY_INITIATED,
        "Break-glass approval session created.",
        metadata = mapOf(
            "approvalSessionId" to session.sessionId,
            "requestHash" to session.requestHash,
            "reason" to session.reason.name,
            "approvedWindowDurationMs" to session.approvedWindowDurationMs.toString(),
            "degradedCauseSnapshotId" to session.degradedCauseSnapshotId,
            "externalRecordId" to externalRecordId
        )
    )

    return session
}

internal fun issueSecurityEmergencyActivationArtifact(
    state: SecurityEmergencyAccessAuthorityState,
    request: EmergencyActivationRequest
): EmergencyActivationArtifact {
    requireArtifactIssuanceTrustState(
        approvalSessionId = request.approvalSession.sessionId
    )

    val currentApprovalSession = state.activeApprovalSession.get()
        ?: throw SecurityException("No active approval session exists.")

    require(currentApprovalSession.sessionId == request.approvalSession.sessionId) {
        "Approval session mismatch."
    }
    require(currentApprovalSession.requestHash == request.approvalSession.requestHash) {
        "Approval session requestHash mismatch."
    }

    EmergencyKeyRotationPolicy.assertArtifactLifetimeAllowed(
        request.artifactLifetimeMs
    )

    state.issuedActivationArtifact.get()?.let { previousArtifact ->
        EmergencyKeyRotationPolicy.assertExtensionUsesFreshBinding(
            previousArtifact = previousArtifact,
            nextBinding = request.keyBinding
        )
    }

    val now = System.currentTimeMillis()

    val artifact = EmergencyActivationArtifact(
        artifactId = UUID.randomUUID().toString(),
        approvalSessionId = request.approvalSession.sessionId,
        requestHash = request.approvalSession.requestHash,
        reason = request.approvalSession.reason,
        audience = request.audience,
        nonce = request.nonce,
        issuedAtEpochMs = now,
        notBeforeEpochMs = now,
        expiresAtEpochMs = now + request.artifactLifetimeMs,
        approvedWindowDurationMs = request.approvalSession.approvedWindowDurationMs,
        trustedBaseOnly = request.approvalSession.trustedBaseOnly,
        keyBinding = request.keyBinding
    )

    val externalRecordId = appendActivationArtifactIssuedAudit(
        auditSink = state.auditSink(),
        artifact = artifact,
        timestampEpochMs = now
    )

    state.artifactRegistry().registerIssuedArtifact(artifact)
    state.issuedActivationArtifact.set(artifact)

    SecurityAuditLog.info(
        EventType.RECOVERY_INITIATED,
        "Break-glass activation artifact issued.",
        metadata = mapOf(
            "artifactId" to artifact.artifactId,
            "approvalSessionId" to artifact.approvalSessionId,
            "reason" to artifact.reason.name,
            "audience" to artifact.audience,
            "expiresAtEpochMs" to artifact.expiresAtEpochMs.toString(),
            "keyId" to artifact.keyBinding.keyId,
            "externalRecordId" to externalRecordId
        )
    )

    return artifact
}

private fun appendApprovalSessionCreatedAudit(
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

private fun appendActivationArtifactIssuedAudit(
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