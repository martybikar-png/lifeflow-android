package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import com.lifeflow.domain.security.EmergencyAuditSinkPort

/**
 * Single authority for temporary break-glass emergency windows.
 *
 * Important:
 * - This is NOT a bypass.
 * - It does NOT override COMPROMISED.
 * - It only creates a narrow trusted-base emergency window.
 * - Standard protected runtime stays blocked unless explicitly activated.
 */
object SecurityEmergencyAccessAuthority {

    private val state = SecurityEmergencyAccessAuthorityState()

    sealed interface AccessResolution {
        data class Approved(
            val window: EmergencyAccessWindow
        ) : AccessResolution

        data object Missing : AccessResolution
        data object Expired : AccessResolution
        data object ReasonMismatch : AccessResolution
    }

    @Synchronized
    internal fun initialize(
        emergencyAuditSink: EmergencyAuditSinkPort,
        emergencyArtifactRegistry: EmergencyArtifactRegistryPort
    ) {
        state.initialize(
            emergencyAuditSink = emergencyAuditSink,
            emergencyArtifactRegistry = emergencyArtifactRegistry
        )
    }

    @Synchronized
    internal fun createApprovalSession(
        request: EmergencyAccessRequest,
        degradedCauseSnapshotId: String,
        firstApproverId: String,
        secondApproverId: String
    ): EmergencyApprovalSession {
        return createSecurityEmergencyApprovalSession(
            state = state,
            request = request,
            degradedCauseSnapshotId = degradedCauseSnapshotId,
            firstApproverId = firstApproverId,
            secondApproverId = secondApproverId
        )
    }

    @Synchronized
    internal fun issueActivationArtifact(
        request: EmergencyActivationRequest
    ): EmergencyActivationArtifact {
        return issueSecurityEmergencyActivationArtifact(
            state = state,
            request = request
        )
    }

    @Synchronized
    internal fun activate(
        artifact: EmergencyActivationArtifact
    ): EmergencyAccessWindow {
        return activateSecurityEmergencyAccessWindow(
            state = state,
            artifact = artifact
        )
    }

    @Synchronized
    internal fun clear(
        reason: String
    ) {
        clearSecurityEmergencyAccessWindow(
            state = state,
            reason = reason
        )
    }

    @Synchronized
    internal fun resolve(
        request: EmergencyAccessRequest?
    ): AccessResolution {
        return resolveSecurityEmergencyAccessWindow(
            state = state,
            request = request
        )
    }

    @Synchronized
    internal fun currentWindow(): EmergencyAccessWindow? {
        return currentSecurityEmergencyAccessWindow(state)
    }
}