package com.lifeflow.security

/**
 * Mapping layer between internal integrity trust models and backend/RPC contract.
 *
 * Rules:
 * - request mapping is structural
 * - response mapping is fail-closed
 * - malformed backend verdict semantics must never silently pass through
 */
internal object IntegrityTrustRpcMapper {

    fun toRpcRequest(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustRpcRequest {
        return IntegrityTrustRpcRequest(
            requestHash = request.requestHash,
            integrityToken = request.integrityToken
        )
    }

    fun fromRpcResponse(
        response: IntegrityTrustRpcResponse,
        expectedRequestHash: String
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustServerVerdictPolicy.normalize(
            response = response,
            expectedRequestHash = expectedRequestHash
        )
    }
}
