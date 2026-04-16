package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityIntegrityClaimsEnforcementPolicyTest {

    @Test
    fun `enforce keeps verified when claims are strong and clean`() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val response = policy.enforce(
            serverResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
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
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertTrue(!response.reason.contains("claimsPolicy="))
    }

    @Test
    fun `enforce degrades verified when device integrity is basic only`() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val response = policy.enforce(
            serverResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                claims = SecurityIntegrityVerdictClaims(
                    appRecognitionVerdict = SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY
                    ),
                    appLicensingVerdict = SecurityIntegrityAppLicensingVerdict.LICENSED,
                    playProtectVerdict = SecurityIntegrityPlayProtectVerdict.NO_ISSUES
                )
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertTrue(response.reason.contains("claimsPolicy=DEGRADED"))
        assertTrue(response.reason.contains("basic only"))
    }

    @Test
    fun `enforce compromises on unlicensed app`() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val response = policy.enforce(
            serverResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                claims = SecurityIntegrityVerdictClaims(
                    appRecognitionVerdict = SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
                    ),
                    appLicensingVerdict = SecurityIntegrityAppLicensingVerdict.UNLICENSED,
                    playProtectVerdict = SecurityIntegrityPlayProtectVerdict.NO_ISSUES
                )
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("claimsPolicy=COMPROMISED"))
        assertTrue(response.reason.contains("UNLICENSED"))
    }

    @Test
    fun `enforce leaves client failsafe response unchanged`() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val response = policy.enforce(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_REQUEST_FAILED: network down",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertEquals("PLAY_INTEGRITY_REQUEST_FAILED: network down", response.reason)
    }

    private fun serverResponse(
        verdict: SecurityIntegrityTrustVerdict,
        claims: SecurityIntegrityVerdictClaims
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = verdict,
            reason = "VERIFIED: SERVER_OK | source=PLAY_INTEGRITY_STANDARD_SERVER",
            requestHashEcho = "hash-123",
            serverTimestampEpochMs = 123456789L,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            claims = claims
        )
    }
}
