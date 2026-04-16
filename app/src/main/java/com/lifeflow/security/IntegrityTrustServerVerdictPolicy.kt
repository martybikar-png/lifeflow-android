package com.lifeflow.security

/**
 * Server-side integrity verdict semantics policy.
 *
 * Rules:
 * - VERIFIED must come with VERIFIED:...
 * - DEGRADED must come with DEGRADED:...
 * - COMPROMISED must come with COMPROMISED:...
 * - requestHash echo must match the original requestHash
 * - malformed or mismatched combinations fail closed to COMPROMISED
 */
internal object IntegrityTrustServerVerdictPolicy {

    private const val VERIFIED_PREFIX = "VERIFIED:"
    private const val DEGRADED_PREFIX = "DEGRADED:"
    private const val COMPROMISED_PREFIX = "COMPROMISED:"
    private const val POLICY_VIOLATION_PREFIX = "SERVER_VERDICT_POLICY_VIOLATION:"

    fun normalize(
        response: IntegrityTrustRpcResponse,
        expectedRequestHash: String
    ): IntegrityTrustVerdictResponse {
        require(expectedRequestHash.isNotBlank()) {
            "expectedRequestHash must not be blank."
        }

        val normalizedReason = response.reason.trim()

        if (response.requestHashEcho != expectedRequestHash) {
            return failClosed(
                "requestHash echo mismatch"
            )
        }

        return when (response.verdict) {
            IntegrityTrustRpcVerdict.VERIFIED ->
                if (normalizedReason.startsWith(VERIFIED_PREFIX)) {
                    serverResponse(
                        verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                        response = response,
                        normalizedReason = normalizedReason
                    )
                } else {
                    failClosed(
                        "VERIFIED verdict requires VERIFIED: reason prefix"
                    )
                }

            IntegrityTrustRpcVerdict.DEGRADED ->
                if (normalizedReason.startsWith(DEGRADED_PREFIX)) {
                    serverResponse(
                        verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                        response = response,
                        normalizedReason = normalizedReason
                    )
                } else {
                    failClosed(
                        "DEGRADED verdict requires DEGRADED: reason prefix"
                    )
                }

            IntegrityTrustRpcVerdict.COMPROMISED ->
                if (normalizedReason.startsWith(COMPROMISED_PREFIX)) {
                    serverResponse(
                        verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                        response = response,
                        normalizedReason = normalizedReason
                    )
                } else {
                    failClosed(
                        "COMPROMISED verdict requires COMPROMISED: reason prefix"
                    )
                }
        }
    }

    private fun serverResponse(
        verdict: SecurityIntegrityTrustVerdict,
        response: IntegrityTrustRpcResponse,
        normalizedReason: String
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = verdict,
            reason = normalizedReason,
            requestHashEcho = response.requestHashEcho,
            serverTimestampEpochMs = response.serverTimestampEpochMs,
            policyVersion = response.policyVersion,
            verdictSource = when (response.verdictSource) {
                IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
                    IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
            }
        )
    }

    private fun failClosed(
        detail: String
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "$POLICY_VIOLATION_PREFIX $detail",
            verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
        )
    }
}
