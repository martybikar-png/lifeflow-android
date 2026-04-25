package com.lifeflow.security

/**
 * Mapping layer between internal integrity trust models and backend/RPC contract.
 *
 * Rules:
 * - request mapping is structural
 * - response mapping is structural plus request-hash echo binding
 * - verdict semantics / fail-closed policy live in SecurityIntegrityTrustAuthority
 *   via SecurityIntegrityServerVerdictPolicy
 *
 * Important:
 * - requestHash echo mismatch is rejected here because only this boundary
 *   simultaneously sees both the expected request hash and the raw RPC response
 */
internal object IntegrityTrustRpcMapper {

    fun toRpcRequest(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustRpcRequest {
        return IntegrityTrustRpcRequest(
            requestHash = request.requestHash,
            requestPayload = request.requestPayload,
            integrityToken = request.integrityToken,
            attestationEvidence = request.attestationEvidence?.let(
                ::mapIntegrityTrustAttestationEvidenceToRpc
            )
        )
    }

    fun fromRpcResponse(
        response: IntegrityTrustRpcResponse,
        expectedRequestHash: String
    ): IntegrityTrustVerdictResponse {
        require(expectedRequestHash.isNotBlank()) {
            "expectedRequestHash must not be blank."
        }

        if (response.requestHashEcho != expectedRequestHash) {
            return IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                reason = "SERVER_VERDICT_METADATA_INVALID: requestHash echo mismatch",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
                decision = IntegrityTrustDecision.LOCK,
                decisionReasonCode = "SERVER_REQUEST_HASH_ECHO_MISMATCH"
            )
        }

        return IntegrityTrustVerdictResponse(
            verdict = mapSecurityIntegrityTrustVerdictFromRpc(response.verdict),
            reason = response.reason.trim(),
            requestHashEcho = response.requestHashEcho,
            requestBindingVerified = response.requestBindingVerified,
            serverTimestampEpochMs = response.serverTimestampEpochMs,
            policyVersion = response.policyVersion,
            verdictSource = mapIntegrityTrustVerdictSourceFromRpc(
                response.verdictSource
            ),
            claims = mapSecurityIntegrityVerdictClaimsFromRpc(response.claims),
            attestationVerification = response.attestationVerification?.let(
                ::mapIntegrityTrustAttestationVerificationFromRpc
            ),
            decision = mapIntegrityTrustDecisionFromRpc(response.decision),
            decisionReasonCode = response.decisionReasonCode
        )
    }
}
