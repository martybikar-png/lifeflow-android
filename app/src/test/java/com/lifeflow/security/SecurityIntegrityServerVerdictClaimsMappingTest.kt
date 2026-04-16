package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityIntegrityServerVerdictClaimsMappingTest {

    @Test
    fun `normalizeRpcResponse maps structured claims into internal verdict response`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalizeRpcResponse(
            response = IntegrityTrustRpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK",
                requestHashEcho = "hash-123",
                serverTimestampEpochMs = System.currentTimeMillis(),
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
                claims = IntegrityTrustRpcClaims(
                    appRecognitionVerdict = IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY,
                        IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY
                    ),
                    appLicensingVerdict = IntegrityTrustRpcAppLicensingVerdict.LICENSED,
                    playProtectVerdict = IntegrityTrustRpcPlayProtectVerdict.NO_ISSUES
                )
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertEquals(
            SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
            response.claims.appRecognitionVerdict
        )
        assertTrue(
            response.claims.deviceRecognitionVerdicts.contains(
                SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
            )
        )
        assertTrue(
            response.claims.deviceRecognitionVerdicts.contains(
                SecurityIntegrityDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY
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
    }

    @Test
    fun `normalizeRpcResponse degrades verified when claims are weaker than verified bar`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalizeRpcResponse(
            response = IntegrityTrustRpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK",
                requestHashEcho = "hash-124",
                serverTimestampEpochMs = System.currentTimeMillis(),
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
                claims = IntegrityTrustRpcClaims(
                    appRecognitionVerdict = IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY
                    ),
                    appLicensingVerdict = IntegrityTrustRpcAppLicensingVerdict.LICENSED,
                    playProtectVerdict = IntegrityTrustRpcPlayProtectVerdict.NO_ISSUES
                )
            ),
            expectedRequestHash = "hash-124"
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertTrue(response.reason.contains("claimsPolicy=DEGRADED"))
    }

    @Test
    fun `normalizeRpcResponse compromises on unlicensed claims`() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = policy.normalizeRpcResponse(
            response = IntegrityTrustRpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK",
                requestHashEcho = "hash-125",
                serverTimestampEpochMs = System.currentTimeMillis(),
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
                claims = IntegrityTrustRpcClaims(
                    appRecognitionVerdict = IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
                    ),
                    appLicensingVerdict = IntegrityTrustRpcAppLicensingVerdict.UNLICENSED,
                    playProtectVerdict = IntegrityTrustRpcPlayProtectVerdict.NO_ISSUES
                )
            ),
            expectedRequestHash = "hash-125"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("claimsPolicy=COMPROMISED"))
        assertTrue(response.reason.contains("UNLICENSED"))
    }
}
