package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import com.lifeflow.domain.security.EmergencyApprovalSession
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

    validateActivationContext(
        approvalSession = approvalSession,
        currentArtifact = currentArtifact,
        artifact = artifact
    )

    val now = System.currentTimeMillis()

    consumeActivationArtifactOrThrow(
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

    val externalRecordId = appendWindowActivatedAudit(
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

private fun validateActivationContext(
    approvalSession: EmergencyApprovalSession,
    currentArtifact: EmergencyActivationArtifact,
    artifact: EmergencyActivationArtifact
) {
    require(currentArtifact.artifactId == artifact.artifactId) {
        "Activation artifact mismatch."
    }
    require(approvalSession.sessionId == artifact.approvalSessionId) {
        "Approval session id mismatch."
    }
    require(approvalSession.requestHash == artifact.requestHash) {
        "Activation requestHash mismatch."
    }
    require(approvalSession.reason == artifact.reason) {
        "Activation reason mismatch."
    }
    require(approvalSession.approvedWindowDurationMs == artifact.approvedWindowDurationMs) {
        "Approved window duration mismatch."
    }
    require(approvalSession.trustedBaseOnly == artifact.trustedBaseOnly) {
        "Trusted-base scope mismatch."
    }
}

private fun consumeActivationArtifactOrThrow(
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

            val externalRecordId = appendActivationArtifactExpiredUnusedAudit(
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

private fun appendActivationArtifactExpiredUnusedAudit(
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

private fun appendWindowActivatedAudit(
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