package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.domain.security.EmergencyAccessReason
import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyActivationKeyBinding
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyAuditEventType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmergencyAuthorityBreakGlassInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        runCatching {
            if (SecurityEmergencyAccessAuthority.currentWindow() != null) {
                SecurityEmergencyAccessAuthority.clear("androidTest teardown")
            }
        }

        LocalEmergencyAuditSink.clear()
        LocalEmergencyArtifactRegistry.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun createApprovalSession_inDegradedState_succeeds_andCapsWindow() {
        initializeBoundary()
        resetTrust(TrustState.DEGRADED, "approval session success")
        LocalEmergencyAuditSink.clear()
        LocalEmergencyArtifactRegistry.clear()

        val request = freshRequest(
            reason = EmergencyAccessReason.MANUAL_BREAK_GLASS_APPROVED,
            requestedDurationMs = 9_999_999L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-1",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        assertEquals(request.reason, session.reason)
        assertEquals(60 * 60_000L, session.approvedWindowDurationMs)
        assertTrue(session.trustedBaseOnly)
        assertTrue(session.requestHash.isNotBlank())

        val records = LocalEmergencyAuditSink.getRecords()
        assertTrue(records.isNotEmpty())
        assertEquals(
            EmergencyAuditEventType.APPROVAL_SESSION_CREATED,
            records.last().record.eventType
        )
    }

    @Test
    fun createApprovalSession_inCompromisedState_isRejected_failClosed() {
        initializeBoundary()
        resetTrust(TrustState.COMPROMISED, "approval session rejected")

        val error = expectSecurityException {
            SecurityEmergencyAccessAuthority.createApprovalSession(
                request = freshRequest(
                    reason = EmergencyAccessReason.LOCKED_OUT_RECOVERY,
                    requestedDurationMs = 10_000L
                ),
                degradedCauseSnapshotId = "snapshot-compromised",
                firstApproverId = "approver-a",
                secondApproverId = "approver-b"
            )
        }

        assertTrue(
            error.message.orEmpty().contains(
                "cannot override COMPROMISED",
                ignoreCase = true
            )
        )
    }

    @Test
    fun fullBreakGlassFlow_activate_resolve_clear_roundTripsCleanly() {
        initializeBoundary()
        resetTrust(TrustState.DEGRADED, "full break-glass flow")
        LocalEmergencyAuditSink.clear()
        LocalEmergencyArtifactRegistry.clear()

        val request = freshRequest(
            reason = EmergencyAccessReason.CRITICAL_HEALTH_ACCESS,
            requestedDurationMs = 20_000L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-flow",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val artifact = SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = EmergencyActivationRequest(
                approvalSession = session,
                audience = "lifeflow-device",
                nonce = "nonce-1",
                requestedAtEpochMs = nowEpochMs(),
                artifactLifetimeMs = 30_000L,
                keyBinding = testKeyBinding("key-1", "thumb-1")
            )
        )

        val window = SecurityEmergencyAccessAuthority.activate(artifact)

        assertNotNull(SecurityEmergencyAccessAuthority.currentWindow())
        assertEquals(TrustState.EMERGENCY_LIMITED, SecurityRuleEngine.getTrustState())
        assertTrue(window.trustedBaseOnly)
        assertEquals(EmergencyAccessReason.CRITICAL_HEALTH_ACCESS, window.reason)

        val resolution = SecurityEmergencyAccessAuthority.resolve(
            freshRequest(
                reason = EmergencyAccessReason.CRITICAL_HEALTH_ACCESS,
                requestedDurationMs = 10_000L
            )
        )

        assertTrue(
            resolution is SecurityEmergencyAccessAuthority.AccessResolution.Approved
        )

        SecurityEmergencyAccessAuthority.clear("androidTest manual clear")

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertEquals(
            SecurityEmergencyAccessAuthority.AccessResolution.Missing,
            SecurityEmergencyAccessAuthority.resolve(
                freshRequest(
                    reason = EmergencyAccessReason.CRITICAL_HEALTH_ACCESS,
                    requestedDurationMs = 10_000L
                )
            )
        )

        val events = LocalEmergencyAuditSink.getRecords().map { it.record.eventType }
        assertTrue(events.contains(EmergencyAuditEventType.APPROVAL_SESSION_CREATED))
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_ISSUED))
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_CONSUMED))
        assertTrue(events.contains(EmergencyAuditEventType.EMERGENCY_WINDOW_CLEARED))
    }

    @Test
    fun issueActivationArtifact_reusingSameBinding_withoutRekey_isRejected() {
        initializeBoundary()
        resetTrust(TrustState.DEGRADED, "rekey rejection")
        LocalEmergencyAuditSink.clear()
        LocalEmergencyArtifactRegistry.clear()

        val request = freshRequest(
            reason = EmergencyAccessReason.VAULT_RECOVERY_READONLY,
            requestedDurationMs = 20_000L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-rekey",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val reusedBinding = testKeyBinding("key-reused", "thumb-reused")

        SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = EmergencyActivationRequest(
                approvalSession = session,
                audience = "lifeflow-device",
                nonce = "nonce-1",
                requestedAtEpochMs = nowEpochMs(),
                artifactLifetimeMs = 30_000L,
                keyBinding = reusedBinding
            )
        )

        val error = expectIllegalArgumentException {
            SecurityEmergencyAccessAuthority.issueActivationArtifact(
                request = EmergencyActivationRequest(
                    approvalSession = session,
                    audience = "lifeflow-device",
                    nonce = "nonce-2",
                    requestedAtEpochMs = nowEpochMs(),
                    artifactLifetimeMs = 30_000L,
                    keyBinding = reusedBinding
                )
            )
        }

        assertTrue(
            error.message.orEmpty().contains(
                "extension without rekey is forbidden",
                ignoreCase = true
            )
        )
    }

    @Test
    fun activateExpiredArtifact_isRejected_andMarkedExpiredUnused() {
        initializeBoundary()
        resetTrust(TrustState.DEGRADED, "expired artifact")
        LocalEmergencyAuditSink.clear()
        LocalEmergencyArtifactRegistry.clear()

        val request = freshRequest(
            reason = EmergencyAccessReason.LOCKED_OUT_RECOVERY,
            requestedDurationMs = 20_000L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-expired",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val artifact = SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = EmergencyActivationRequest(
                approvalSession = session,
                audience = "lifeflow-device",
                nonce = "nonce-expired",
                requestedAtEpochMs = nowEpochMs(),
                artifactLifetimeMs = 1L,
                keyBinding = testKeyBinding("key-expired", "thumb-expired")
            )
        )

        Thread.sleep(20L)

        val error = expectSecurityException {
            SecurityEmergencyAccessAuthority.activate(artifact)
        }

        assertTrue(
            error.message.orEmpty().contains(
                "expired before use",
                ignoreCase = true
            )
        )
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityEmergencyAccessAuthority.currentWindow() != null)

        val events = LocalEmergencyAuditSink.getRecords().map { it.record.eventType }
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_EXPIRED_UNUSED))
    }

    private fun initializeBoundary() {
        EmergencyAuthorityBoundaryBootstrap.start(
            applicationContext = appContext,
            isInstrumentation = true
        )
    }

    private fun resetTrust(
        state: TrustState,
        reason: String
    ) {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = state,
            reason = reason
        )
    }

    private fun nowEpochMs(): Long = System.currentTimeMillis()

    private fun freshRequest(
        reason: EmergencyAccessReason,
        requestedDurationMs: Long
    ): EmergencyAccessRequest {
        return EmergencyAccessRequest(
            reason = reason,
            requestedAtEpochMs = nowEpochMs() - 1_000L,
            requestedDurationMs = requestedDurationMs
        )
    }

    private fun testKeyBinding(
        keyId: String,
        thumbprint: String
    ) = EmergencyActivationKeyBinding(
        keyId = keyId,
        confirmationKeyThumbprint = thumbprint,
        algorithm = "EC",
        createdAtEpochMs = nowEpochMs() - 1_000L,
        hardwareBacked = true,
        exportable = false
    )

    private fun expectSecurityException(
        block: () -> Unit
    ): SecurityException {
        try {
            block()
        } catch (e: SecurityException) {
            return e
        }

        fail("Expected SecurityException")
        throw IllegalStateException("Unreachable")
    }

    private fun expectIllegalArgumentException(
        block: () -> Unit
    ): IllegalArgumentException {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            return e
        }

        fail("Expected IllegalArgumentException")
        throw IllegalStateException("Unreachable")
    }
}
