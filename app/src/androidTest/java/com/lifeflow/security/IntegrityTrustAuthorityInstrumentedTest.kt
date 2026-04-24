package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.audit.SecurityAuditLog
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegrityTrustAuthorityInstrumentedTest {

    @After
    fun tearDown() {
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun clientFailsafeDegradedVerdict_setsDegradedTrust_andWritesTrustEvent() {
        resetSecurityState("client failsafe degraded test")

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_NOT_CONFIGURED"
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        val entry = latestAuditEvent(SecurityAuditLog.EventType.TRUST_DEGRADED)
        assertEquals("DEGRADED", entry.metadata["verdict"])
        assertEquals("CLIENT_FAILSAFE", entry.metadata["verdictSource"])
        assertTrue(
            entry.metadata["reasonSummary"]
                .orEmpty()
                .contains("PLAY_INTEGRITY_NOT_CONFIGURED")
        )
    }

    @Test
    fun serverVerdict_missingAttestationVerification_failsClosedToCompromised_andWritesTrustEvent() {
        resetSecurityState("missing attestation test")

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "SERVER_OK",
                requestHashEcho = "req-attestation-missing",
                serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val entry = latestAuditEvent(SecurityAuditLog.EventType.TRUST_COMPROMISED)
        assertEquals("COMPROMISED", entry.metadata["verdict"])
        assertEquals("CLIENT_FAILSAFE", entry.metadata["verdictSource"])
        assertTrue(
            entry.metadata["reasonSummary"]
                .orEmpty()
                .contains("SERVER_VERDICT_")
        )
    }

    @Test
    fun serverVerdict_staleMetadata_failsClosedToCompromised_andWritesTrustEvent() {
        resetSecurityState("stale metadata test")

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "SERVER_OK",
                requestHashEcho = "req-stale-metadata",
                serverTimestampEpochMs = FIXED_NOW_EPOCH_MS - 600_000L,
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
                attestationVerification = IntegrityTrustAttestationVerification(
                    chainVerdict = IntegrityTrustAttestationChainVerdict.VERIFIED,
                    challengeVerdict = IntegrityTrustAttestationChallengeVerdict.MATCHED,
                    rootVerdict = IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED,
                    revocationVerdict = IntegrityTrustAttestationRevocationVerdict.CLEAN,
                    appBindingVerdict = IntegrityTrustAttestationAppBindingVerdict.MATCHED,
                    detail = "androidTest valid attestation"
                )
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val entry = latestAuditEvent(SecurityAuditLog.EventType.TRUST_COMPROMISED)
        assertEquals("COMPROMISED", entry.metadata["verdict"])
        assertEquals("CLIENT_FAILSAFE", entry.metadata["verdictSource"])
        assertTrue(
            entry.metadata["reasonSummary"]
                .orEmpty()
                .contains("SERVER_VERDICT_METADATA_INVALID")
        )
    }

    @Test
    fun clientFailsafeCompromisedVerdict_setsCompromisedTrust_andWritesTrustEvent() {
        resetSecurityState("client failsafe compromised test")

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                reason = "CLIENT_FAILSAFE_COMPROMISED"
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val entry = latestAuditEvent(SecurityAuditLog.EventType.TRUST_COMPROMISED)
        assertEquals("COMPROMISED", entry.metadata["verdict"])
        assertEquals("CLIENT_FAILSAFE", entry.metadata["verdictSource"])
        assertTrue(
            entry.metadata["reasonSummary"]
                .orEmpty()
                .contains("CLIENT_FAILSAFE_COMPROMISED")
        )
    }

    private fun resetSecurityState(reason: String) {
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = reason
        )
    }

    private fun latestAuditEvent(
        eventType: SecurityAuditLog.EventType
    ): SecurityAuditLog.AuditEntry {
        return SecurityAuditLog.getEntries()
            .last { it.eventType == eventType }
    }

    private companion object {
        private const val FIXED_NOW_EPOCH_MS = 1_800_000_000_000L
    }
}
