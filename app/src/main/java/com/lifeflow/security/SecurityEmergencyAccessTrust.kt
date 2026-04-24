package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal fun requireApprovalSessionTrustState(
    request: EmergencyAccessRequest
) {
    val trustState = currentTrustState()

    if (trustState == TrustState.COMPROMISED) {
        SecurityAuditLog.critical(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass approval session rejected because trust state is COMPROMISED.",
            metadata = mapOf(
                "reason" to request.reason.name
            )
        )
        throw SecurityException("Break-glass cannot override COMPROMISED.")
    }

    if (trustState != TrustState.DEGRADED) {
        SecurityAuditLog.warning(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass approval session rejected because trust state is not DEGRADED.",
            metadata = mapOf(
                "trustState" to trustState.name,
                "reason" to request.reason.name
            )
        )
        throw SecurityException("Break-glass approval requires DEGRADED trust state.")
    }
}

internal fun requireArtifactIssuanceTrustState(
    approvalSessionId: String
) {
    val trustState = currentTrustState()

    if (trustState != TrustState.DEGRADED) {
        SecurityAuditLog.warning(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass activation artifact rejected because trust state is not DEGRADED.",
            metadata = mapOf(
                "trustState" to trustState.name,
                "approvalSessionId" to approvalSessionId
            )
        )
        throw SecurityException("Activation artifact issuance requires DEGRADED trust state.")
    }
}

internal fun requireActivationTrustState(
    artifact: EmergencyActivationArtifact
) {
    val trustState = currentTrustState()

    if (trustState == TrustState.COMPROMISED) {
        SecurityAuditLog.critical(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass activation rejected because trust state is COMPROMISED.",
            metadata = mapOf(
                "artifactId" to artifact.artifactId,
                "reason" to artifact.reason.name
            )
        )
        throw SecurityException("Break-glass cannot override COMPROMISED.")
    }

    if (trustState != TrustState.DEGRADED) {
        SecurityAuditLog.warning(
            EventType.BREAK_GLASS_REJECTED,
            "Break-glass activation rejected because trust state is not DEGRADED.",
            metadata = mapOf(
                "artifactId" to artifact.artifactId,
                "trustState" to trustState.name
            )
        )
        throw SecurityException("Break-glass activation requires DEGRADED trust state.")
    }
}

internal fun enterEmergencyLimitedTrustState(
    reasonName: String
) {
    SecurityRuleEngine.setTrustState(
        TrustState.EMERGENCY_LIMITED,
        reason = "BREAK_GLASS_APPROVED: $reasonName"
    )
}

internal fun restoreDegradedTrustAfterEmergencyWindow(
    reason: String
) {
    if (currentTrustState() == TrustState.EMERGENCY_LIMITED) {
        SecurityRuleEngine.setTrustState(
            TrustState.DEGRADED,
            reason = reason
        )
    }
}

private fun currentTrustState(): TrustState {
    return SecurityRuleEngine.getTrustState()
}