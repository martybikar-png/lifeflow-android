package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.domain.security.EmergencyAccessReason
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyAuditEventType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmergencyAuthorityBreakGlassInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    private val support = EmergencyAuthorityBreakGlassTestSupport

    @After
    fun tearDown() {
        runCatching {
            if (SecurityEmergencyAccessAuthority.currentWindow() != null) {
                SecurityEmergencyAccessAuthority.clear("androidTest teardown")
            }
        }

        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun createApprovalSession_inDegradedState_succeeds_andCapsWindow() {
        support.initializeBoundary(appContext)
        support.resetTrust(TrustState.DEGRADED, "approval session success")
        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()

        val request = support.freshRequest(
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

        val records = InstrumentationEmergencyAuditSink.getRecords()
        assertTrue(records.isNotEmpty())
        assertEquals(
            EmergencyAuditEventType.APPROVAL_SESSION_CREATED,
            records.last().record.eventType
        )
    }

    @Test
    fun createApprovalSession_inCompromisedState_isRejected_failClosed() {
        support.initializeBoundary(appContext)
        support.resetTrust(TrustState.COMPROMISED, "approval session rejected")

        val error = support.expectSecurityException {
            SecurityEmergencyAccessAuthority.createApprovalSession(
                request = support.freshRequest(
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
        support.initializeBoundary(appContext)
        support.resetTrust(TrustState.DEGRADED, "full break-glass flow")
        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()

        val request = support.freshRequest(
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
                requestedAtEpochMs = support.nowEpochMs(),
                artifactLifetimeMs = 30_000L,
                keyBinding = support.testKeyBinding("key-1", "thumb-1")
            )
        )

        val window = SecurityEmergencyAccessAuthority.activate(artifact)

        assertNotNull(SecurityEmergencyAccessAuthority.currentWindow())
        assertEquals(TrustState.EMERGENCY_LIMITED, SecurityRuleEngine.getTrustState())
        assertTrue(window.trustedBaseOnly)
        assertEquals(EmergencyAccessReason.CRITICAL_HEALTH_ACCESS, window.reason)

        val resolution = SecurityEmergencyAccessAuthority.resolve(
            support.freshRequest(
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
                support.freshRequest(
                    reason = EmergencyAccessReason.CRITICAL_HEALTH_ACCESS,
                    requestedDurationMs = 10_000L
                )
            )
        )

        val events = InstrumentationEmergencyAuditSink.getRecords().map { it.record.eventType }
        assertTrue(events.contains(EmergencyAuditEventType.APPROVAL_SESSION_CREATED))
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_ISSUED))
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_CONSUMED))
        assertTrue(events.contains(EmergencyAuditEventType.EMERGENCY_WINDOW_CLEARED))
    }

    @Test
    fun issueActivationArtifact_reusingSameBinding_withoutRekey_isRejected() {
        support.initializeBoundary(appContext)
        support.resetTrust(TrustState.DEGRADED, "rekey rejection")
        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()

        val request = support.freshRequest(
            reason = EmergencyAccessReason.VAULT_RECOVERY_READONLY,
            requestedDurationMs = 20_000L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-rekey",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val reusedBinding = support.testKeyBinding("key-reused", "thumb-reused")

        SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = EmergencyActivationRequest(
                approvalSession = session,
                audience = "lifeflow-device",
                nonce = "nonce-1",
                requestedAtEpochMs = support.nowEpochMs(),
                artifactLifetimeMs = 30_000L,
                keyBinding = reusedBinding
            )
        )

        val error = support.expectIllegalArgumentException {
            SecurityEmergencyAccessAuthority.issueActivationArtifact(
                request = EmergencyActivationRequest(
                    approvalSession = session,
                    audience = "lifeflow-device",
                    nonce = "nonce-2",
                    requestedAtEpochMs = support.nowEpochMs(),
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
        support.initializeBoundary(appContext)
        support.resetTrust(TrustState.DEGRADED, "expired artifact")
        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()

        val request = support.freshRequest(
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
                requestedAtEpochMs = support.nowEpochMs(),
                artifactLifetimeMs = 1L,
                keyBinding = support.testKeyBinding("key-expired", "thumb-expired")
            )
        )

        Thread.sleep(20L)

        val error = support.expectSecurityException {
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

        val events = InstrumentationEmergencyAuditSink.getRecords().map { it.record.eventType }
        assertTrue(events.contains(EmergencyAuditEventType.ACTIVATION_ARTIFACT_EXPIRED_UNUSED))
    }
}