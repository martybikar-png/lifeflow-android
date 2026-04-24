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
    private val maxConsumedRequestHashes: Int = 256
) {
    init {
        require(maxServerVerdictSkewMs > 0L) { "maxServerVerdictSkewMs must be > 0." }
        require(policyVersionPrefix.isNotBlank()) { "policyVersionPrefix must not be blank." }
        require(maxConsumedRequestHashes > 0) { "maxConsumedRequestHashes must be > 0." }
    }

    private val consumedServerVerdictRequestHashes = linkedSetOf<String>()
    private val claimsEnforcementPolicy = SecurityIntegrityClaimsEnforcementPolicy()
    private val attestationPolicy = SecurityIntegrityServerAttestationPolicy()
    private val rpcMapper = SecurityIntegrityServerRpcMapper()

    fun clear() {
        consumedServerVerdictRequestHashes.clear()
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
                    ?: return metadataFailClosed("missing requestHashEcho")
                val serverTimestampEpochMs = response.serverTimestampEpochMs
                    ?: return metadataFailClosed("missing serverTimestampEpochMs")
                val policyVersion = response.policyVersion
                    ?: return metadataFailClosed("missing policyVersion")

                if (response.requestBindingVerified != true) {
                    return metadataFailClosed("request binding was not verified by server")
                }

                if (!policyVersion.startsWith(policyVersionPrefix)) {
                    return metadataFailClosed(
                        "invalid policyVersion format ($policyVersion)"
                    )
                }

                val skewMs = abs(nowEpochMs - serverTimestampEpochMs)
                if (skewMs > maxServerVerdictSkewMs) {
                    return metadataFailClosed(
                        "stale server verdict metadata (skewMs=$skewMs)"
                    )
                }

                if (!rememberConsumedServerVerdictRequestHash(requestHashEcho)) {
                    return metadataFailClosed(
                        "duplicate server verdict requestHashEcho ($requestHashEcho)"
                    )
                }

                if (!isDecisionCompatible(
                        verdict = response.verdict,
                        decision = response.decision
                    )
                ) {
                    return metadataFailClosed(
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
            return metadataFailClosed("requestHash echo mismatch")
        }

        return normalize(
            response = rpcMapper.map(response),
            nowEpochMs = System.currentTimeMillis()
        )
    }

    private fun isDecisionCompatible(
        verdict: SecurityIntegrityTrustVerdict,
        decision: IntegrityTrustDecision
    ): Boolean {
        return when (verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED ->
                decision == IntegrityTrustDecision.ALLOW ||
                    decision == IntegrityTrustDecision.STEP_UP

            SecurityIntegrityTrustVerdict.DEGRADED ->
                decision == IntegrityTrustDecision.STEP_UP ||
                    decision == IntegrityTrustDecision.DEGRADED ||
                    decision == IntegrityTrustDecision.DENY

            SecurityIntegrityTrustVerdict.COMPROMISED ->
                decision == IntegrityTrustDecision.DENY ||
                    decision == IntegrityTrustDecision.LOCK
        }
    }

    private fun rememberConsumedServerVerdictRequestHash(
        requestHashEcho: String
    ): Boolean {
        if (requestHashEcho in consumedServerVerdictRequestHashes) {
            return false
        }

        while (consumedServerVerdictRequestHashes.size >= maxConsumedRequestHashes) {
            val oldest = consumedServerVerdictRequestHashes.first()
            consumedServerVerdictRequestHashes.remove(oldest)
        }

        consumedServerVerdictRequestHashes.add(requestHashEcho)
        return true
    }

    private fun metadataFailClosed(
        detail: String
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "SERVER_VERDICT_METADATA_INVALID: $detail",
            verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            decision = IntegrityTrustDecision.LOCK,
            decisionReasonCode = "SERVER_METADATA_INVALID"
        )
    }
}