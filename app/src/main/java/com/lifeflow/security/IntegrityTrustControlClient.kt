package com.lifeflow.security

import io.grpc.ManagedChannel

/**
 * Control-side client boundary for external integrity trust verdict RPCs.
 *
 * Purpose:
 * - keep verdict RPC behavior separate from transport/channel wiring
 * - make future generated stubs sit behind a dedicated RPC adapter
 * - remain fail-closed until the real RPC contract exists
 */
internal interface IntegrityTrustControlClient {
    fun requestVerdict(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustVerdictResponse
}

internal class GrpcIntegrityTrustControlClient(
    private val rpcClient: IntegrityTrustRpcClient
) : IntegrityTrustControlClient {

    constructor(channel: ManagedChannel) : this(
        rpcClient = UnconfiguredGrpcIntegrityTrustRpcClient(channel)
    )

    override fun requestVerdict(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustVerdictResponse {
        val rpcRequest = IntegrityTrustRpcMapper.toRpcRequest(request)
        val rpcResponse = rpcClient.requestVerdict(rpcRequest)

        return IntegrityTrustRpcMapper.fromRpcResponse(
            response = rpcResponse,
            expectedRequestHash = request.requestHash
        )
    }
}
