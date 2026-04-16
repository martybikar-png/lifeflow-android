package com.lifeflow.security

import io.grpc.ManagedChannel

/**
 * Narrow RPC adapter boundary for integrity trust verdict calls.
 *
 * Purpose:
 * - isolate future generated gRPC/proto stub usage
 * - keep GrpcIntegrityTrustControlClient focused on mapping + orchestration only
 * - preserve fail-closed behavior until the real backend contract exists
 */
internal interface IntegrityTrustRpcClient {
    fun requestVerdict(
        request: IntegrityTrustRpcRequest
    ): IntegrityTrustRpcResponse
}

internal class UnconfiguredGrpcIntegrityTrustRpcClient(
    private val channel: ManagedChannel
) : IntegrityTrustRpcClient {

    override fun requestVerdict(
        request: IntegrityTrustRpcRequest
    ): IntegrityTrustRpcResponse {
        channel
        request

        throw SecurityException(
            "gRPC integrity trust verdict RPC contract is not configured yet. Integrity trust remains fail-closed."
        )
    }
}
