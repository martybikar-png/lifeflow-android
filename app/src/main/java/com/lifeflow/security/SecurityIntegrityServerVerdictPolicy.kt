package com.lifeflow.security

import kotlin.math.abs

/**
 * Security policy for server-sourced integrity verdict metadata.
 *
 * Purpose:
 * - keep freshness / replay / policyVersion rules out of SecurityRuleEngine
 * - keep server verdict hygiene in one testable place
 * - fail closed when server verdict metadata is stale, malformed, replayed,
 *   when explicit request binding verification is missing,
 *   or when server attestation verification is missing/invalid
 */
internal class SecurityIntegrityServerVerdictPolicy(
    private val maxServerVerdictSkewMs: Long = 5 * 60_000L,
    private val policyVersionPrefix: String = "policy-v",
    maxConsumedRequestHashes: Int = 256
) {
    init {
        require(maxServerVerdictSkewMs > 0L) { "maxServerVerdictSkewMs must be > 0." }
        require(policyVersionPrefix.isNotBlank()) { "policyVersionPrefix must not be blank." }
    }

    private val replayGuard = SecurityIntegrityServerVerdictReplayGuard(
        maxConsumedRequestHashes = maxConsumedRequestHashes
    )
    private val claimsEnforcementPolicy = SecurityIntegrityClaimsEnforcementPolicy()
    private val attestationPolicy = SecurityIntegrityServerAttestationPolicy()
    private val rpcMapper = SecurityIntegrityServerRpcMapper()

    fun clear() {
        replayGuard.clear()
    }

    fun normalize(
        response: IntegrityTrustVerdictResponse,
        nowEpochMs: Long
    ): IntegrityTrustVerdictResponse {
        require(nowEpochMs > 0L) { "nowEpochMs must be > 0." }

        return when (response.verdictSource) {
            IntegrityTrustVerdictSource.CLIENT_FAILSAFE -> {
                response.copy(
                    reason = buildString {
                        append(response.reason)
                        append(" | source=")
                        append(response.verdictSource)
                        append(" | decision=")
                        append(response.decision)
                        response.decisionReasonCode?.let {
                            append(" | decisionCode=")
                            append(it)
                        }
                    }
                )
            }

            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER -> {
                val requestHashEcho = response.requestHashEcho
                    ?: return securityIntegrityServerVerdictMetadataFailClosed("missing requestHashEcho")
                val serverTimestampEpochMs = response.serverTimestampEpochMs
                    ?: return securityIntegrityServerVerdictMetadataFailClosed("missing serverTimestampEpochMs")
                val policyVersion = response.policyVersion
                    ?: return securityIntegrityServerVerdictMetadataFailClosed("missing policyVersion")

                if (response.requestBindingVerified != true) {
                    return securityIntegrityServerVerdictMetadataFailClosed("request binding was not verified by server")
                }

                if (!policyVersion.startsWith(policyVersionPrefix)) {
                    return securityIntegrityServerVerdictMetadataFailClosed(
                        "invalid policyVersion format ($policyVersion)"
                    )
                }

                val skewMs = abs(nowEpochMs - serverTimestampEpochMs)
                if (skewMs > maxServerVerdictSkewMs) {
                    return securityIntegrityServerVerdictMetadataFailClosed(
                        "stale server verdict metadata (skewMs=$skewMs)"
                    )
                }

                if (!replayGuard.remember(requestHashEcho)) {
                    return securityIntegrityServerVerdictMetadataFailClosed(
                        "duplicate server verdict requestHashEcho ($requestHashEcho)"
                    )
                }

                if (!securityIntegrityServerDecisionCompatible(
                        verdict = response.verdict,
                        decision = response.decision
                    )
                ) {
                    return securityIntegrityServerVerdictMetadataFailClosed(
                        "invalid zero-trust decision ${response.decision} for verdict ${response.verdict}"
                    )
                }

                val responseWithMetadata = response.copy(
                    reason = buildString {
                        append(response.reason)
                        append(" | source=")
                        append(response.verdictSource)
                        append(" | requestBindingVerified=")
                        append(response.requestBindingVerified)
                        append(" | policy=")
                        append(policyVersion)
                        append(" | serverTs=")
                        append(serverTimestampEpochMs)
                        append(" | requestHashEcho=")
                        append(requestHashEcho)
                        append(" | decision=")
                        append(response.decision)
                        response.decisionReasonCode?.let {
                            append(" | decisionCode=")
                            append(it)
                        }
                    }
                )

                val responseWithAttestationPolicy = attestationPolicy.enforce(
                    responseWithMetadata
                )
                if (responseWithAttestationPolicy.verdictSource ==
                    IntegrityTrustVerdictSource.CLIENT_FAILSAFE
                ) {
                    return responseWithAttestationPolicy
                }

                claimsEnforcementPolicy.enforce(responseWithAttestationPolicy)
            }
        }
    }

    fun normalizeRpcResponse(
        response: IntegrityTrustRpcResponse,
        expectedRequestHash: String
    ): IntegrityTrustVerdictResponse {
        require(expectedRequestHash.isNotBlank()) {
            "expectedRequestHash must not be blank."
        }

        if (response.requestHashEcho != expectedRequestHash) {
            return securityIntegrityServerVerdictMetadataFailClosed("requestHash echo mismatch")
        }

        return normalize(
            response = rpcMapper.map(response),
            nowEpochMs = System.currentTimeMillis()
        )
    }
}
