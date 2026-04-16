package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityRuleEngineReplayProtectionTest {

    @Before
    fun resetSecurityBaseline() {
        setBaseline(TrustState.DEGRADED)
    }

    @Test
    fun `duplicate server request hash echo fails closed`() {
        setBaseline(TrustState.DEGRADED)

        val requestHash = "replay-duplicate-hash"

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 60_000L
        )
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 61_000L
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("SERVER_VERDICT_METADATA_INVALID"))
        assertTrue(lastEvent.reason.contains("duplicate server verdict requestHashEcho"))
    }

    @Test
    fun `force reset clears consumed server request hash replay cache`() {
        setBaseline(TrustState.DEGRADED)

        val requestHash = "replay-force-reset-hash"

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 60_000L
        )
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "Replay cache reset for adversarial suite"
        )

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 61_000L
        )

        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("requestHashEcho=$requestHash"))
    }

    @Test
    fun `recover after vault reset clears consumed server request hash replay cache`() {
        setBaseline(TrustState.DEGRADED)

        val requestHash = "replay-recovery-hash"

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 60_000L
        )
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "Simulated compromise before vault reset"
        )
        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        SecurityRuleEngine.recoverAfterVaultReset(
            reason = "Replay cache should be cleared after recovery"
        )
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        SecurityIntegrityTrustAuthority.reportVerdictResponse(
            response = freshServerVerifiedResponse(requestHash),
            nowEpochMs = 61_000L
        )

        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("requestHashEcho=$requestHash"))
    }

    private fun setBaseline(state: TrustState) {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = state,
            reason = "SecurityRuleEngineReplayProtectionTest baseline -> $state"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    private fun freshServerVerifiedResponse(
        requestHashEcho: String
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "VERIFIED: SERVER_OK",
            requestHashEcho = requestHashEcho,
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

