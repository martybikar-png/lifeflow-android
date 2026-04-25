package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.UUID

internal fun activateSecurityEmergencyAccessWindow(
    state: SecurityEmergencyAccessAuthorityState,
    artifact: EmergencyActivationArtifact
): EmergencyAccessWindow {
    requireActivationTrustState(artifact)

    if (state.activeWindow.get() != null) {
        SecurityAuditLog.warning(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass activation rejected because a window is already active.",
            metadata = mapOf(
                "artifactId" to artifact.artifactId
            )
        )
        throw SecurityException("An emergency window is already active.")
    }

    val approvalSession = state.activeApprovalSession.get()
        ?: throw SecurityException("No active approval session exists.")

    val currentArtifact = state.issuedActivationArtifact.get()
        ?: throw SecurityException("No issued activation artifact exists.")

    validateSecurityEmergencyActivationContext(
        approvalSession = approvalSession,
        currentArtifact = currentArtifact,
        artifact = artifact
    )

    val now = System.currentTimeMillis()

    consumeSecurityEmergencyActivationArtifactOrThrow(
        state = state,
        currentArtifact = currentArtifact,
        artifact = artifact,
        consumedAtEpochMs = now
    )

    val window = EmergencyAccessWindow(
        windowId = UUID.randomUUID().toString(),
        reason = artifact.reason,
        startedAtEpochMs = now,
        expiresAtEpochMs = now + artifact.approvedWindowDurationMs,
        trustedBaseOnly = artifact.trustedBaseOnly
    )

    val externalRecordId = appendSecurityEmergencyWindowActivatedAudit(
        auditSink = state.auditSink(),
        artifact = artifact,
        window = window,
        timestampEpochMs = now
    )

    state.activeWindow.set(window)
    state.issuedActivationArtifact.compareAndSet(currentArtifact, null)

    enterEmergencyLimitedTrustState(
        reasonName = artifact.reason.name
    )

    SecurityAuditLog.warning(
        EventType.BREAK_GLASS_APPROVED,
        "Break-glass emergency window activated.",
        metadata = mapOf(
            "windowId" to window.windowId,
            "artifactId" to artifact.artifactId,
            "approvalSessionId" to artifact.approvalSessionId,
            "requestHash" to artifact.requestHash,
            "reason" to window.reason.name,
            "startedAtEpochMs" to window.startedAtEpochMs.toString(),
            "expiresAtEpochMs" to window.expiresAtEpochMs.toString(),
            "trustedBaseOnly" to window.trustedBaseOnly.toString(),
            "externalRecordId" to externalRecordId
        )
    )

    return window
}
