package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityIntegrityAuditExplainability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityIntegrityAuditExplainabilityTest {

    @Before
    fun resetState() {
        SecurityAuditLog.clear()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "SecurityIntegrityAuditExplainabilityTest baseline"
        )
        SecurityRuleEngine.clearAudit()
    }

    @Test
    fun `snapshot builds enriched metadata for verified server verdict`() {
        val snapshot = SecurityIntegrityAuditExplainability.snapshot(
            strongVerifiedResponse()
        )

        assertEquals(SecurityAuditLog.EventType.TRUST_VERIFIED, snapshot.eventType)
        assertTrue(snapshot.message.contains("accepted"))
        assertEquals("PLAY_INTEGRITY_STANDARD_SERVER", snapshot.metadata["verdictSource"])
        assertEquals("SERVER_VERDICT_ACCEPTED", snapshot.metadata["decisionPath"])
        assertEquals("policy-v1", snapshot.metadata["policyVersion"])
        assertEquals("PLAY_RECOGNIZED", snapshot.metadata["appRecognitionVerdict"])
        assertTrue(
            snapshot.metadata["deviceRecognitionVerdicts"]
                .orEmpty()
                .contains("MEETS_DEVICE_INTEGRITY")
        )
        assertTrue(
            snapshot.metadata["requestHashEchoShort"]
                .orEmpty()
                .contains("...")
        )
    }

    @Test
    fun `security rule engine writes enriched audit entry for integrity verdict response`() {
        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = strongVerifiedResponse(),
            nowEpochMs = 60_000L
        )

        val entry = findEntryByDecisionPath("SERVER_VERDICT_ACCEPTED")

        assertEquals(SecurityAuditLog.EventType.TRUST_VERIFIED, entry.eventType)
        assertEquals(SecurityAuditLog.Severity.INFO, entry.severity)
        assertTrue(entry.message.contains("accepted"))
        assertEquals("PLAY_INTEGRITY_STANDARD_SERVER", entry.metadata["verdictSource"])
        assertEquals("SERVER_VERDICT_ACCEPTED", entry.metadata["decisionPath"])
        assertEquals("policy-v1", entry.metadata["policyVersion"])
        assertEquals("PLAY_RECOGNIZED", entry.metadata["appRecognitionVerdict"])
        assertEquals("LICENSED", entry.metadata["appLicensingVerdict"])
        assertEquals("NO_ISSUES", entry.metadata["playProtectVerdict"])
    }

    @Test
    fun `security rule engine writes compromised audit entry for metadata invalid response`() {
        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = strongVerifiedResponse(),
            nowEpochMs = 1_000L + 301_000L
        )

        val entry = findEntryByDecisionPath("SERVER_METADATA_INVALID")

        assertEquals(SecurityAuditLog.EventType.TRUST_COMPROMISED, entry.eventType)
        assertEquals(SecurityAuditLog.Severity.CRITICAL, entry.severity)
        assertEquals("SERVER_METADATA_INVALID", entry.metadata["decisionPath"])
        assertEquals("CLIENT_FAILSAFE", entry.metadata["verdictSource"])
    }

    private fun findEntryByDecisionPath(
        decisionPath: String
    ): SecurityAuditLog.AuditEntry {
        return SecurityAuditLog.getEntries().last { entry ->
            entry.metadata["decisionPath"] == decisionPath
        }
    }

    private fun strongVerifiedResponse(): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "VERIFIED: SERVER_OK",
            requestHashEcho = "12345678abcdef0012345678abcdef00",
            serverTimestampEpochMs = 1_000L,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            claims = SecurityIntegrityVerdictClaims(
                appRecognitionVerdict = SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
                deviceRecognitionVerdicts = linkedSetOf(
                    SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY,
                    SecurityIntegrityDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY
                ),
                appLicensingVerdict = SecurityIntegrityAppLicensingVerdict.LICENSED,
                playProtectVerdict = SecurityIntegrityPlayProtectVerdict.NO_ISSUES
            )
        )
    }
}

