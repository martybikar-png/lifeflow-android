package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import com.lifeflow.domain.security.EmergencyKeyRotationPolicy
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

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

    private const val MAX_WINDOW_MS: Long = 60 * 60_000L

    private val activeApprovalSession = AtomicReference<EmergencyApprovalSession?>(null)
    private val issuedActivationArtifact = AtomicReference<EmergencyActivationArtifact?>(null)
    private val activeWindow = AtomicReference<EmergencyAccessWindow?>(null)

    private val emergencyAuditSinkRef = AtomicReference<EmergencyAuditSinkPort?>(null)
    private val emergencyArtifactRegistryRef = AtomicReference<EmergencyArtifactRegistryPort?>(null)

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
        emergencyAuditSinkRef.set(emergencyAuditSink)
        emergencyArtifactRegistryRef.set(emergencyArtifactRegistry)
    }

    private fun auditSink(): EmergencyAuditSinkPort {
        return emergencyAuditSinkRef.get()
            ?: error("SecurityEmergencyAccessAuthority is not initialized with EmergencyAuditSinkPort.")
    }

    private fun artifactRegistry(): EmergencyArtifactRegistryPort {
        return emergencyArtifactRegistryRef.get()
            ?: error("SecurityEmergencyAccessAuthority is not initialized with EmergencyArtifactRegistryPort.")
    }

    @Synchronized
    internal fun createApprovalSession(
        request: EmergencyAccessRequest,
        degradedCauseSnapshotId: String,
        firstApproverId: String,
        secondApproverId: String
    ): EmergencyApprovalSession {
        requireApprovalSessionTrustState(request)

        val boundedDurationMs = request.requestedDurationMs.coerceAtMost(MAX_WINDOW_MS)
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

        val externalRecordId = auditSink().append(
            EmergencyAuditRecord(
                timestampEpochMs = now,
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

        activeApprovalSession.set(session)
        issuedActivationArtifact.set(null)

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

    @Synchronized
    internal fun issueActivationArtifact(
        request: EmergencyActivationRequest
    ): EmergencyActivationArtifact {
        requireArtifactIssuanceTrustState(
            approvalSessionId = request.approvalSession.sessionId
        )

        val currentApprovalSession = activeApprovalSession.get()
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

        issuedActivationArtifact.get()?.let { previousArtifact ->
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

        val externalRecordId = auditSink().append(
            EmergencyAuditRecord(
                timestampEpochMs = now,
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

        artifactRegistry().registerIssuedArtifact(artifact)
        issuedActivationArtifact.set(artifact)

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

    @Synchronized
    internal fun activate(
        artifact: EmergencyActivationArtifact
    ): EmergencyAccessWindow {
        requireActivationTrustState(artifact)

        if (activeWindow.get() != null) {
            SecurityAuditLog.warning(
                EventType.BREAK_GLASS_REJECTED,
                "Break-glass activation rejected because a window is already active.",
                metadata = mapOf(
                    "artifactId" to artifact.artifactId
                )
            )
            throw SecurityException("An emergency window is already active.")
        }

        val approvalSession = activeApprovalSession.get()
            ?: throw SecurityException("No active approval session exists.")

        val currentArtifact = issuedActivationArtifact.get()
            ?: throw SecurityException("No issued activation artifact exists.")

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

        val now = System.currentTimeMillis()

        when (
            artifactRegistry().consumeIssuedArtifact(
                artifactId = artifact.artifactId,
                consumedAtEpochMs = now
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
                issuedActivationArtifact.compareAndSet(currentArtifact, null)

                val externalRecordId = auditSink().append(
                    EmergencyAuditRecord(
                        timestampEpochMs = now,
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

        val window = EmergencyAccessWindow(
            windowId = UUID.randomUUID().toString(),
            reason = artifact.reason,
            startedAtEpochMs = now,
            expiresAtEpochMs = now + artifact.approvedWindowDurationMs,
            trustedBaseOnly = artifact.trustedBaseOnly
        )

        val externalRecordId = auditSink().append(
            EmergencyAuditRecord(
                timestampEpochMs = now,
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

        activeWindow.set(window)
        issuedActivationArtifact.compareAndSet(currentArtifact, null)

        SecurityRuleEngine.setTrustState(
            TrustState.EMERGENCY_LIMITED,
            reason = "BREAK_GLASS_APPROVED: ${artifact.reason.name}"
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

    @Synchronized
    internal fun clear(
        reason: String
    ) {
        val window = activeWindow.getAndSet(null) ?: return
        issuedActivationArtifact.set(null)
        activeApprovalSession.set(null)

        val now = System.currentTimeMillis()

        val externalRecordId = auditSink().append(
            EmergencyAuditRecord(
                timestampEpochMs = now,
                eventType = EmergencyAuditEventType.EMERGENCY_WINDOW_CLEARED,
                windowId = window.windowId,
                reason = window.reason,
                trustedBaseOnly = window.trustedBaseOnly,
                detail = "Break-glass emergency window cleared.",
                metadata = mapOf(
                    "clearReason" to reason,
                    "expiresAtEpochMs" to window.expiresAtEpochMs.toString()
                )
            )
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

    @Synchronized
    internal fun resolve(
        request: EmergencyAccessRequest?
    ): AccessResolution {
        if (request == null) {
            return AccessResolution.Missing
        }

        val window = activeWindow.get() ?: return AccessResolution.Missing
        val now = System.currentTimeMillis()

        if (!window.isActiveAt(now)) {
            expire(window)
            return AccessResolution.Expired
        }

        if (window.reason != request.reason) {
            return AccessResolution.ReasonMismatch
        }

        return AccessResolution.Approved(window)
    }

    @Synchronized
    internal fun currentWindow(): EmergencyAccessWindow? {
        val window = activeWindow.get() ?: return null
        val now = System.currentTimeMillis()

        if (!window.isActiveAt(now)) {
            expire(window)
            return null
        }

        return window
    }

    @Synchronized
    private fun expire(
        window: EmergencyAccessWindow
    ) {
        activeWindow.compareAndSet(window, null)
        issuedActivationArtifact.set(null)
        activeApprovalSession.set(null)

        val now = System.currentTimeMillis()

        val externalRecordId = auditSink().append(
            EmergencyAuditRecord(
                timestampEpochMs = now,
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

    private fun requireApprovalSessionTrustState(
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

    private fun requireArtifactIssuanceTrustState(
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

    private fun requireActivationTrustState(
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

    private fun restoreDegradedTrustAfterEmergencyWindow(
        reason: String
    ) {
        if (currentTrustState() == TrustState.EMERGENCY_LIMITED) {
            SecurityRuleEngine.setTrustState(
                TrustState.DEGRADED,
                reason = reason
            )
        }
    }

    private fun currentTrustState(): TrustState =
        SecurityRuleEngine.getTrustState()

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return buildString(digest.size * 2) {
            digest.forEach { byte ->
                append("%02x".format(byte))
            }
        }
    }
}
