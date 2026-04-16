package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrityTrustRpcMapperTest {

    @Test
    fun `toRpcRequest maps request hash and token unchanged`() {
        val rpcRequest = IntegrityTrustRpcMapper.toRpcRequest(
            IntegrityTrustVerdictRequest(
                requestHash = "hash-123",
                integrityToken = "token-abc"
            )
        )

        assertEquals("hash-123", rpcRequest.requestHash)
        assertEquals("token-abc", rpcRequest.integrityToken)
    }

    @Test
    fun `fromRpcResponse maps verified verdict with canonical reason and metadata`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK",
                requestHashEcho = "hash-123"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response.verdict)
        assertEquals("VERIFIED: SERVER_OK", response.reason)
        assertEquals("hash-123", response.requestHashEcho)
        assertEquals(123456789L, response.serverTimestampEpochMs)
        assertEquals("policy-v1", response.policyVersion)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            response.verdictSource
        )
    }

    @Test
    fun `fromRpcResponse maps degraded verdict with canonical reason and metadata`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.DEGRADED,
                reason = "DEGRADED: DEVICE_RISK",
                requestHashEcho = "hash-123"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response.verdict)
        assertEquals("DEGRADED: DEVICE_RISK", response.reason)
        assertEquals("hash-123", response.requestHashEcho)
        assertEquals(123456789L, response.serverTimestampEpochMs)
        assertEquals("policy-v1", response.policyVersion)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            response.verdictSource
        )
    }

    @Test
    fun `fromRpcResponse maps compromised verdict with canonical reason and metadata`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.COMPROMISED,
                reason = "COMPROMISED: APP_TAMPER",
                requestHashEcho = "hash-123"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertEquals("COMPROMISED: APP_TAMPER", response.reason)
        assertEquals("hash-123", response.requestHashEcho)
        assertEquals(123456789L, response.serverTimestampEpochMs)
        assertEquals("policy-v1", response.policyVersion)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            response.verdictSource
        )
    }

    @Test
    fun `fromRpcResponse fails closed when verified verdict has degraded reason`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "DEGRADED: DEVICE_RISK",
                requestHashEcho = "hash-123"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("SERVER_VERDICT_POLICY_VIOLATION"))
        assertTrue(response.reason.contains("VERIFIED verdict requires VERIFIED"))
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
    }

    @Test
    fun `fromRpcResponse fails closed when request hash echo mismatches`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK",
                requestHashEcho = "hash-other"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("SERVER_VERDICT_POLICY_VIOLATION"))
        assertTrue(response.reason.contains("requestHash echo mismatch"))
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
    }

    @Test
    fun `fromRpcResponse fails closed when compromised verdict has unclassified reason`() {
        val response = IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse(
                verdict = IntegrityTrustRpcVerdict.COMPROMISED,
                reason = "SERVER_UNKNOWN",
                requestHashEcho = "hash-123"
            ),
            expectedRequestHash = "hash-123"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("SERVER_VERDICT_POLICY_VIOLATION"))
        assertTrue(response.reason.contains("COMPROMISED verdict requires COMPROMISED"))
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
    }

    private fun rpcResponse(
        verdict: IntegrityTrustRpcVerdict,
        reason: String,
        requestHashEcho: String
    ): IntegrityTrustRpcResponse {
        return IntegrityTrustRpcResponse(
            verdict = verdict,
            reason = reason,
            requestHashEcho = requestHashEcho,
            serverTimestampEpochMs = 123456789L,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
        )
    }
}
