package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
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

    val session = EmergencyApprovalSession(
        sessionId = UUID.randomUUID().toString(),
        requestHash = buildSecurityEmergencyApprovalRequestHash(
            request = request,
            boundedDurationMs = boundedDurationMs,
            degradedCauseSnapshotId = degradedCauseSnapshotId
        ),
        reason = request.reason,
        requestedAtEpochMs = request.requestedAtEpochMs,
        approvedAtEpochMs = now,
        approvedWindowDurationMs = boundedDurationMs,
        trustedBaseOnly = true,
        degradedCauseSnapshotId = degradedCauseSnapshotId,
        firstApproverId = firstApproverId,
        secondApproverId = secondApproverId
    )

    val externalRecordId = appendSecurityEmergencyApprovalSessionCreatedAudit(
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
    validateSecurityEmergencyActivationArtifactIssuance(
        state = state,
        request = request
    )

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

    val externalRecordId = appendSecurityEmergencyActivationArtifactIssuedAudit(
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
