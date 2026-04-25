package com.lifeflow.security

/**
 * Backend/RPC-side request/response contract for integrity trust verdict exchange.
 *
 * Purpose:
 * - isolate future generated gRPC/proto models from internal runtime models
 * - keep transport/client wiring independent from domain-facing verdict models
 */
internal data class IntegrityTrustRpcRequest(
    val requestHash: String,
    val requestPayload: String,
    val integrityToken: String,
    val attestationEvidence: IntegrityTrustRpcAttestationEvidence? = null
) {
    init {
        require(requestHash.isNotBlank()) { "RPC requestHash must not be blank." }
        require(requestPayload.isNotBlank()) { "RPC requestPayload must not be blank." }
        require(integrityToken.isNotBlank()) { "RPC integrityToken must not be blank." }
    }
}

internal data class IntegrityTrustRpcResponse(
    val verdict: IntegrityTrustRpcVerdict,
    val reason: String,
    val requestHashEcho: String,
    val requestBindingVerified: Boolean = false,
    val serverTimestampEpochMs: Long,
    val policyVersion: String,
    val verdictSource: IntegrityTrustRpcVerdictSource,
    val claims: IntegrityTrustRpcClaims = IntegrityTrustRpcClaims(),
    val attestationVerification: IntegrityTrustRpcAttestationVerification? = null,
    val decision: IntegrityTrustRpcDecision = defaultIntegrityTrustRpcDecisionFor(verdict),
    val decisionReasonCode: String? = null
) {
    init {
        require(reason.isNotBlank()) { "RPC reason must not be blank." }
        require(requestHashEcho.isNotBlank()) { "RPC requestHashEcho must not be blank." }
        require(serverTimestampEpochMs > 0L) { "RPC serverTimestampEpochMs must be > 0." }
        require(policyVersion.isNotBlank()) { "RPC policyVersion must not be blank." }
        decisionReasonCode?.let {
            require(it.isNotBlank()) { "RPC decisionReasonCode must not be blank when present." }
        }
    }
}
