package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal fun clearSecurityEmergencyAccessWindow(
    state: SecurityEmergencyAccessAuthorityState,
    reason: String
) {
    val window = state.activeWindow.getAndSet(null) ?: return
    state.issuedActivationArtifact.set(null)
    state.activeApprovalSession.set(null)

    val now = System.currentTimeMillis()

    val externalRecordId = appendWindowClearedAudit(
        auditSink = state.auditSink(),
        window = window,
        clearReason = reason,
        timestampEpochMs = now
    )

    restoreDegradedTrustAfterEmergencyWindow(
        reason = "BREAK_GLASS_CLEARED: $reason"
    )

    SecurityAuditLog.info(
        EventType.BREAK_GLASS_CLEARED,
        "Break-glass emergency window cleared.",
        metadata = mapOf(
            "windowId" to window.windowId,
            "reason" to reason,
            "externalRecordId" to externalRecordId
        )
    )
}

internal fun resolveSecurityEmergencyAccessWindow(
    state: SecurityEmergencyAccessAuthorityState,
    request: EmergencyAccessRequest?
): SecurityEmergencyAccessAuthority.AccessResolution {
    if (request == null) {
        return SecurityEmergencyAccessAuthority.AccessResolution.Missing
    }

    val window = state.activeWindow.get()
        ?: return SecurityEmergencyAccessAuthority.AccessResolution.Missing

    val now = System.currentTimeMillis()

    if (!window.isActiveAt(now)) {
        expireSecurityEmergencyAccessWindow(
            state = state,
            window = window
        )
        return SecurityEmergencyAccessAuthority.AccessResolution.Expired
    }

    if (window.reason != request.reason) {
        return SecurityEmergencyAccessAuthority.AccessResolution.ReasonMismatch
    }

    return SecurityEmergencyAccessAuthority.AccessResolution.Approved(window)
}

internal fun currentSecurityEmergencyAccessWindow(
    state: SecurityEmergencyAccessAuthorityState
): EmergencyAccessWindow? {
    val window = state.activeWindow.get() ?: return null
    val now = System.currentTimeMillis()

    if (!window.isActiveAt(now)) {
        expireSecurityEmergencyAccessWindow(
            state = state,
            window = window
        )
        return null
    }

    return window
}

private fun expireSecurityEmergencyAccessWindow(
    state: SecurityEmergencyAccessAuthorityState,
    window: EmergencyAccessWindow
) {
    state.activeWindow.compareAndSet(window, null)
    state.issuedActivationArtifact.set(null)
    state.activeApprovalSession.set(null)

    val now = System.currentTimeMillis()

    val externalRecordId = appendWindowExpiredAudit(
        auditSink = state.auditSink(),
        window = window,
        timestampEpochMs = now
    )

    restoreDegradedTrustAfterEmergencyWindow(
        reason = "BREAK_GLASS_EXPIRED: ${window.reason.name}"
    )

    SecurityAuditLog.warning(
        EventType.BREAK_GLASS_EXPIRED,
        "Break-glass emergency window expired.",
        metadata = mapOf(
            "windowId" to window.windowId,
            "reason" to window.reason.name,
            "expiresAtEpochMs" to window.expiresAtEpochMs.toString(),
            "externalRecordId" to externalRecordId
        )
    )
}

private fun appendWindowClearedAudit(
    auditSink: EmergencyAuditSinkPort,
    window: EmergencyAccessWindow,
    clearReason: String,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.EMERGENCY_WINDOW_CLEARED,
            windowId = window.windowId,
            reason = window.reason,
            trustedBaseOnly = window.trustedBaseOnly,
            detail = "Break-glass emergency window cleared.",
            metadata = mapOf(
                "clearReason" to clearReason,
                "expiresAtEpochMs" to window.expiresAtEpochMs.toString()
            )
        )
    )
}

private fun appendWindowExpiredAudit(
    auditSink: EmergencyAuditSinkPort,
    window: EmergencyAccessWindow,
    timestampEpochMs: Long
): String {
    return auditSink.append(
        EmergencyAuditRecord(
            timestampEpochMs = timestampEpochMs,
            eventType = EmergencyAuditEventType.EMERGENCY_WINDOW_EXPIRED,
            windowId = window.windowId,
            reason = window.reason,
            trustedBaseOnly = window.trustedBaseOnly,
            detail = "Break-glass emergency window expired.",
            metadata = mapOf(
                "expiresAtEpochMs" to window.expiresAtEpochMs.toString()
            )
        )
    )
}