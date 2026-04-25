package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyApprovalSession

internal fun validateSecurityEmergencyActivationContext(
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
