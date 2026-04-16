package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrityTrustControlClientTest {

    @Test
    fun `requestVerdict maps request into rpc adapter and maps verified response back`() {
        var capturedRequest: IntegrityTrustRpcRequest? = null

        val client = GrpcIntegrityTrustControlClient(
            rpcClient = object : IntegrityTrustRpcClient {
                override fun requestVerdict(
                    request: IntegrityTrustRpcRequest
                ): IntegrityTrustRpcResponse {
                    capturedRequest = request
                    return IntegrityTrustRpcResponse(
                        verdict = IntegrityTrustRpcVerdict.VERIFIED,
                        reason = "VERIFIED: SERVER_OK",
                        requestHashEcho = request.requestHash,
                        serverTimestampEpochMs = 123456789L,
                        policyVersion = "policy-v1",
                        verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
                    )
                }
            }
        )

        val response = client.requestVerdict(
            IntegrityTrustVerdictRequest(
                requestHash = "hash-123",
                integrityToken = "token-abc"
            )
        )

        assertEquals("hash-123", capturedRequest?.requestHash)
        assertEquals("token-abc", capturedRequest?.integrityToken)
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
    fun `requestVerdict fails closed when backend semantics are invalid`() {
        val client = GrpcIntegrityTrustControlClient(
            rpcClient = object : IntegrityTrustRpcClient {
                override fun requestVerdict(
                    request: IntegrityTrustRpcRequest
                ): IntegrityTrustRpcResponse {
                    return IntegrityTrustRpcResponse(
                        verdict = IntegrityTrustRpcVerdict.VERIFIED,
                        reason = "DEGRADED: DEVICE_RISK",
                        requestHashEcho = request.requestHash,
                        serverTimestampEpochMs = 123456789L,
                        policyVersion = "policy-v1",
                        verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
                    )
                }
            }
        )

        val response = client.requestVerdict(
            IntegrityTrustVerdictRequest(
                requestHash = "hash-123",
                integrityToken = "token-abc"
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertTrue(response.reason.contains("SERVER_VERDICT_POLICY_VIOLATION"))
        assertNull(response.requestHashEcho)
        assertNull(response.serverTimestampEpochMs)
        assertNull(response.policyVersion)
        assertEquals(IntegrityTrustVerdictSource.CLIENT_FAILSAFE, response.verdictSource)
    }

    @Test
    fun `requestVerdict fails closed when request hash echo mismatches`() {
        val client = GrpcIntegrityTrustControlClient(
            rpcClient = object : IntegrityTrustRpcClient {
                override fun requestVerdict(
                    request: IntegrityTrustRpcRequest
                ): IntegrityTrustRpcResponse {
                    return IntegrityTrustRpcResponse(
                        verdict = IntegrityTrustRpcVerdict.VERIFIED,
                        reason = "VERIFIED: SERVER_OK",
                        requestHashEcho = "different-hash",
                        serverTimestampEpochMs = 123456789L,
                        policyVersion = "policy-v1",
                        verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
                    )
                }
            }
        )

        val response = client.requestVerdict(
            IntegrityTrustVerdictRequest(
                requestHash = "hash-123",
                integrityToken = "token-abc"
            )
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
    fun `requestVerdict propagates fail closed rpc client exception`() {
        val client = GrpcIntegrityTrustControlClient(
            rpcClient = object : IntegrityTrustRpcClient {
                override fun requestVerdict(
                    request: IntegrityTrustRpcRequest
                ): IntegrityTrustRpcResponse {
                    throw SecurityException("RPC unavailable")
                }
            }
        )

        val error = try {
            client.requestVerdict(
                IntegrityTrustVerdictRequest(
                    requestHash = "hash-123",
                    integrityToken = "token-abc"
                )
            )
            throw AssertionError("Expected SecurityException")
        } catch (e: SecurityException) {
            e
        }

        assertTrue(error.message.orEmpty().contains("RPC unavailable"))
    }
}
