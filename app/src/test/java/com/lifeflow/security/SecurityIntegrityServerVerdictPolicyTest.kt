package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityIntegrityServerVerdictPolicyTest {

    @Test
    fun `normalize accepts fresh server verdict and enriches reason`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 60_000L
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertEquals("hash-123", response.requestHashEcho)
        assertEquals(1_000L, response.serverTimestampEpochMs)
        assertEquals("policy-v1", response.policyVersion)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            response.verdictSource
        )
        assertEquals(
            SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
            response.claims.appRecognitionVerdict
        )
        assertTrue(
            response.claims.deviceRecognitionVerdicts.contains(
                SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
            )
        )
        assertEquals(
            SecurityIntegrityAppLicensingVerdict.LICENSED,
            response.claims.appLicensingVerdict
        )
        assertEquals(
            SecurityIntegrityPlayProtectVerdict.NO_ISSUES,
            response.claims.playProtectVerdict
        )
        assertTrue(response.reason.contains("requestHashEcho=hash-123"))
        assertTrue(response.reason.contains("policy-v1"))
    }

    @Test
    fun `normalize fails closed on stale server verdict`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 1_000L + 301_000L
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("SERVER_VERDICT_METADATA_INVALID"))
        assertTrue(response.reason.contains("stale server verdict metadata"))
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
    }

    @Test
    fun `normalize fails closed on duplicate request hash echo`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val first = policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 60_000L
        )
        val second = policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 61_000L
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, first.verdict)
        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, second.verdict)
        assertTrue(second.reason.contains("duplicate server verdict requestHashEcho"))
    }

    @Test
    fun `clear resets duplicate protection state`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 60_000L
        )
        policy.clear()

        val response = policy.normalize(
            response = freshServerVerifiedResponse("hash-123"),
            nowEpochMs = 61_000L
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertTrue(response.reason.contains("requestHashEcho=hash-123"))
    }

    @Test
    fun `normalize accepts client failsafe without server metadata`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalize(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_REQUEST_FAILED: network down",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
            ),
            nowEpochMs = 60_000L
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
        assertTrue(response.reason.contains("CLIENT_FAILSAFE"))
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
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
