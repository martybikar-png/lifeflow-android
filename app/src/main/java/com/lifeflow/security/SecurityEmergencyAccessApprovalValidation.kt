package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyKeyRotationPolicy

internal fun validateSecurityEmergencyActivationArtifactIssuance(
    state: SecurityEmergencyAccessAuthorityState,
    request: EmergencyActivationRequest
) {
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
}
