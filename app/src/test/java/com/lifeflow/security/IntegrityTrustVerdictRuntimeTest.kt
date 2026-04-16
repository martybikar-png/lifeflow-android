package com.lifeflow.security

import com.lifeflow.security.integrity.PlayIntegrityVerifier
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrityTrustVerdictRuntimeTest {

    @Test
    fun `requestServerVerdict passes request hash and token to external verdict authority`() = runTest {
        var capturedTokenRequestHash: String? = null
        var capturedCloudProjectNumber: Long? = null
        var capturedExternalRequest: IntegrityTrustVerdictRequest? = null

        val runtime = IntegrityTrustVerdictRuntime(
            cloudProjectNumber = 12345L,
            generateRequestHash = { "request-hash-123" },
            requestIntegrityToken = { requestHash, cloudProjectNumber ->
                capturedTokenRequestHash = requestHash
                capturedCloudProjectNumber = cloudProjectNumber
                PlayIntegrityVerifier.IntegrityResult.Success("token-abc")
            },
            requestExternalVerdict = { request ->
                capturedExternalRequest = request
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                    reason = "VERIFIED: SERVER_OK",
                    requestHashEcho = request.requestHash,
                    serverTimestampEpochMs = 123456789L,
                    policyVersion = "policy-v1",
                    verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
                )
            }
        )

        val response = runtime.requestServerVerdict("startup-payload")

        assertEquals("request-hash-123", capturedTokenRequestHash)
        assertEquals(12345L, capturedCloudProjectNumber)
        assertEquals("request-hash-123", capturedExternalRequest?.requestHash)
        assertEquals("token-abc", capturedExternalRequest?.integrityToken)
        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertEquals("VERIFIED: SERVER_OK", response.reason)
        assertEquals("request-hash-123", response.requestHashEcho)
        assertEquals(123456789L, response.serverTimestampEpochMs)
        assertEquals("policy-v1", response.policyVersion)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            response.verdictSource
        )
    }

    @Test
    fun `requestServerVerdict degrades when play integrity request fails`() = runTest {
        var externalVerdictCalled = false

        val runtime = IntegrityTrustVerdictRuntime(
            cloudProjectNumber = 12345L,
            generateRequestHash = { "request-hash-123" },
            requestIntegrityToken = { _, _ ->
                PlayIntegrityVerifier.IntegrityResult.Failure("network down")
            },
            requestExternalVerdict = {
                externalVerdictCalled = true
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                    reason = "SHOULD_NOT_HAPPEN"
                )
            }
        )

        val response = runtime.requestServerVerdict("startup-payload")

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertTrue(response.reason.contains("PLAY_INTEGRITY_REQUEST_FAILED"))
        assertTrue(response.reason.contains("network down"))
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertFalse(externalVerdictCalled)
    }

    @Test
    fun `requestServerVerdict degrades when play integrity is not configured`() = runTest {
        var externalVerdictCalled = false

        val runtime = IntegrityTrustVerdictRuntime(
            cloudProjectNumber = 0L,
            generateRequestHash = { "request-hash-123" },
            requestIntegrityToken = { _, _ ->
                PlayIntegrityVerifier.IntegrityResult.NotConfigured
            },
            requestExternalVerdict = {
                externalVerdictCalled = true
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                    reason = "SHOULD_NOT_HAPPEN"
                )
            }
        )

        val response = runtime.requestServerVerdict("startup-payload")

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertEquals("PLAY_INTEGRITY_NOT_CONFIGURED", response.reason)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertFalse(externalVerdictCalled)
    }
}
