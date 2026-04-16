package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessReason
import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyActivationKeyBinding
import com.lifeflow.domain.security.EmergencyActivationRequest
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import com.lifeflow.domain.security.EmergencyAuditEventType
import com.lifeflow.domain.security.EmergencyAuditRecord
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class SecurityEmergencyAccessAuthorityTest {

    private lateinit var auditSink: FakeEmergencyAuditSink
    private lateinit var artifactRegistry: FakeEmergencyArtifactRegistry

    @Before
    fun resetSecurityBaseline() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "SecurityEmergencyAccessAuthorityTest baseline"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()

        resetEmergencyAuthorityStateForTests()

        auditSink = FakeEmergencyAuditSink()
        artifactRegistry = FakeEmergencyArtifactRegistry()

        SecurityEmergencyAccessAuthority.initialize(
            emergencyAuditSink = auditSink,
            emergencyArtifactRegistry = artifactRegistry
        )
    }

    @Test
    fun `createApprovalSession rejects compromised trust state`() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.COMPROMISED,
            reason = "Compromised break-glass test"
        )
        SecurityRuleEngine.clearAudit()

        val error = expectSecurityException {
            SecurityEmergencyAccessAuthority.createApprovalSession(
                request = emergencyAccessRequest(),
                degradedCauseSnapshotId = "snapshot-1",
                firstApproverId = "approver-a",
                secondApproverId = "approver-b"
            )
        }

        assertTrue(error.message.orEmpty().contains("COMPROMISED"))
    }

    @Test
    fun `createApprovalSession in degraded trust creates trusted-base session`() {
        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = emergencyAccessRequest(),
            degradedCauseSnapshotId = "snapshot-1",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        assertTrue(session.trustedBaseOnly)
        assertEquals("snapshot-1", session.degradedCauseSnapshotId)
        assertEquals(
            EmergencyAuditEventType.APPROVAL_SESSION_CREATED,
            auditSink.records.last().eventType
        )
    }

    @Test
    fun `activate with valid artifact enters emergency limited and resolve returns approved`() {
        val approvalSession = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = emergencyAccessRequest(),
            degradedCauseSnapshotId = "snapshot-1",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val artifact = SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = emergencyActivationRequest(approvalSession)
        )

        val window = SecurityEmergencyAccessAuthority.activate(artifact)

        assertEquals(TrustState.EMERGENCY_LIMITED, SecurityRuleEngine.getTrustState())
        assertEquals(
            EmergencyAuditEventType.ACTIVATION_ARTIFACT_CONSUMED,
            auditSink.records.last().eventType
        )

        val resolution = SecurityEmergencyAccessAuthority.resolve(
            request = EmergencyAccessRequest(
                reason = window.reason,
                requestedAtEpochMs = 2_000L,
                requestedDurationMs = 1_000L
            )
        )

        when (resolution) {
            is SecurityEmergencyAccessAuthority.AccessResolution.Approved ->
                assertEquals(window.windowId, resolution.window.windowId)

            SecurityEmergencyAccessAuthority.AccessResolution.Missing ->
                fail("Expected Approved but got Missing.")

            SecurityEmergencyAccessAuthority.AccessResolution.Expired ->
                fail("Expected Approved but got Expired.")

            SecurityEmergencyAccessAuthority.AccessResolution.ReasonMismatch ->
                fail("Expected Approved but got ReasonMismatch.")
        }
    }

    @Test
    fun `clear after active window restores degraded trust`() {
        val approvalSession = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = emergencyAccessRequest(),
            degradedCauseSnapshotId = "snapshot-1",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val artifact = SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = emergencyActivationRequest(approvalSession)
        )

        SecurityEmergencyAccessAuthority.activate(artifact)
        assertEquals(TrustState.EMERGENCY_LIMITED, SecurityRuleEngine.getTrustState())

        SecurityEmergencyAccessAuthority.clear(
            reason = "test clear"
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertNull(SecurityEmergencyAccessAuthority.currentWindow())
        assertEquals(
            EmergencyAuditEventType.EMERGENCY_WINDOW_CLEARED,
            auditSink.records.last().eventType
        )
    }

    private fun emergencyAccessRequest(): EmergencyAccessRequest =
        EmergencyAccessRequest(
            reason = emergencyReason(),
            requestedAtEpochMs = 1_000L,
            requestedDurationMs = 5_000L
        )

    private fun emergencyActivationRequest(
        approvalSession: EmergencyApprovalSession,
        keyId: String = "key-1"
    ): EmergencyActivationRequest =
        EmergencyActivationRequest(
            approvalSession = approvalSession,
            audience = "lifeflow-tests",
            nonce = "nonce-$keyId",
            requestedAtEpochMs = 2_000L,
            artifactLifetimeMs = 30_000L,
            keyBinding = emergencyKeyBinding(keyId)
        )

    private fun emergencyKeyBinding(
        keyId: String
    ): EmergencyActivationKeyBinding =
        EmergencyActivationKeyBinding(
            keyId = keyId,
            confirmationKeyThumbprint = "$keyId-thumbprint",
            algorithm = "EC",
            createdAtEpochMs = 1_000L,
            hardwareBacked = true,
            exportable = false
        )

    private fun emergencyReason(): EmergencyAccessReason =
        enumValues<EmergencyAccessReason>().first()

    private fun expectSecurityException(
        block: () -> Unit
    ): SecurityException {
        return try {
            block()
            throw AssertionError("Expected SecurityException.")
        } catch (e: SecurityException) {
            e
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resetEmergencyAuthorityStateForTests() {
        listOf(
            "activeApprovalSession",
            "issuedActivationArtifact",
            "activeWindow"
        ).forEach { fieldName ->
            val field = SecurityEmergencyAccessAuthority::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            val reference = field.get(null) as AtomicReference<Any?>
            reference.set(null)
        }
    }

    private class FakeEmergencyAuditSink : EmergencyAuditSinkPort {
        val records = mutableListOf<EmergencyAuditRecord>()

        override fun append(record: EmergencyAuditRecord): String {
            records += record
            return "record-${records.size}"
        }
    }

    private class FakeEmergencyArtifactRegistry : EmergencyArtifactRegistryPort {
        private data class Entry(
            val artifact: EmergencyActivationArtifact,
            var consumed: Boolean = false,
            var expiredUnused: Boolean = false
        )

        private val entries = linkedMapOf<String, Entry>()

        override fun registerIssuedArtifact(
            artifact: EmergencyActivationArtifact
        ) {
            entries[artifact.artifactId] = Entry(artifact)
        }

        override fun consumeIssuedArtifact(
            artifactId: String,
            consumedAtEpochMs: Long
        ): EmergencyArtifactConsumptionStatus {
            val entry = entries[artifactId]
                ?: return EmergencyArtifactConsumptionStatus.MISSING

            if (entry.consumed) {
                return EmergencyArtifactConsumptionStatus.ALREADY_CONSUMED
            }

            if (entry.expiredUnused || consumedAtEpochMs > entry.artifact.expiresAtEpochMs) {
                entry.expiredUnused = true
                return EmergencyArtifactConsumptionStatus.EXPIRED_UNUSED
            }

            entry.consumed = true
            return EmergencyArtifactConsumptionStatus.CONSUMED
        }

        override fun markArtifactExpiredUnused(
            artifactId: String,
            reason: String,
            expiredAtEpochMs: Long
        ) {
            entries[artifactId]?.expiredUnused = true
        }
    }
}
