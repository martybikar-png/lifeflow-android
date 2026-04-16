package com.lifeflow.security

import kotlin.math.abs

/**
 * Security policy for server-sourced integrity verdict metadata.
 *
 * Purpose:
 * - keep freshness / replay / policyVersion rules out of SecurityRuleEngine
 * - keep server verdict hygiene in one testable place
 * - fail closed when server verdict metadata is stale, malformed, or replayed
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
                    reason = "${response.reason} | source=${response.verdictSource}"
                )
            }

            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER -> {
                val requestHashEcho = response.requestHashEcho
                    ?: return metadataFailClosed("missing requestHashEcho")
                val serverTimestampEpochMs = response.serverTimestampEpochMs
                    ?: return metadataFailClosed("missing serverTimestampEpochMs")
                val policyVersion = response.policyVersion
                    ?: return metadataFailClosed("missing policyVersion")

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

                claimsEnforcementPolicy.enforce(
                    response.copy(
                        reason = buildString {
                            append(response.reason)
                            append(" | source=")
                            append(response.verdictSource)
                            append(" | policy=")
                            append(policyVersion)
                            append(" | serverTs=")
                            append(serverTimestampEpochMs)
                            append(" | requestHashEcho=")
                            append(requestHashEcho)
                        }
                    )
                )
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

        val mappedVerdict = when (response.verdict) {
            IntegrityTrustRpcVerdict.VERIFIED -> SecurityIntegrityTrustVerdict.VERIFIED
            IntegrityTrustRpcVerdict.DEGRADED -> SecurityIntegrityTrustVerdict.DEGRADED
            IntegrityTrustRpcVerdict.COMPROMISED -> SecurityIntegrityTrustVerdict.COMPROMISED
        }

        val mappedSource = when (response.verdictSource) {
            IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
                IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
        }

        return normalize(
            response = IntegrityTrustVerdictResponse(
                verdict = mappedVerdict,
                reason = response.reason.trim(),
                requestHashEcho = response.requestHashEcho,
                serverTimestampEpochMs = response.serverTimestampEpochMs,
                policyVersion = response.policyVersion,
                verdictSource = mappedSource,
                claims = mapClaims(response.claims)
            ),
            nowEpochMs = System.currentTimeMillis()
        )
    }

    private fun mapClaims(
        claims: IntegrityTrustRpcClaims
    ): SecurityIntegrityVerdictClaims {
        return SecurityIntegrityVerdictClaims(
            appRecognitionVerdict = claims.appRecognitionVerdict?.let { rpcValue ->
                when (rpcValue) {
                    IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED ->
                        SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED

                    IntegrityTrustRpcAppRecognitionVerdict.UNRECOGNIZED_VERSION ->
                        SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION

                    IntegrityTrustRpcAppRecognitionVerdict.UNEVALUATED ->
                        SecurityIntegrityAppRecognitionVerdict.UNEVALUATED
                }
            },
            deviceRecognitionVerdicts = claims.deviceRecognitionVerdicts.mapTo(linkedSetOf()) { rpcValue ->
                when (rpcValue) {
                    IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY ->
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY

                    IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY ->
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY

                    IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY ->
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY

                    IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_VIRTUAL_INTEGRITY ->
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_VIRTUAL_INTEGRITY
                }
            },
            appLicensingVerdict = claims.appLicensingVerdict?.let { rpcValue ->
                when (rpcValue) {
                    IntegrityTrustRpcAppLicensingVerdict.LICENSED ->
                        SecurityIntegrityAppLicensingVerdict.LICENSED

                    IntegrityTrustRpcAppLicensingVerdict.UNLICENSED ->
                        SecurityIntegrityAppLicensingVerdict.UNLICENSED

                    IntegrityTrustRpcAppLicensingVerdict.UNEVALUATED ->
                        SecurityIntegrityAppLicensingVerdict.UNEVALUATED
                }
            },
            playProtectVerdict = claims.playProtectVerdict?.let { rpcValue ->
                when (rpcValue) {
                    IntegrityTrustRpcPlayProtectVerdict.NO_ISSUES ->
                        SecurityIntegrityPlayProtectVerdict.NO_ISSUES

                    IntegrityTrustRpcPlayProtectVerdict.NO_DATA ->
                        SecurityIntegrityPlayProtectVerdict.NO_DATA

                    IntegrityTrustRpcPlayProtectVerdict.POSSIBLE_RISK ->
                        SecurityIntegrityPlayProtectVerdict.POSSIBLE_RISK

                    IntegrityTrustRpcPlayProtectVerdict.MEDIUM_RISK ->
                        SecurityIntegrityPlayProtectVerdict.MEDIUM_RISK

                    IntegrityTrustRpcPlayProtectVerdict.HIGH_RISK ->
                        SecurityIntegrityPlayProtectVerdict.HIGH_RISK

                    IntegrityTrustRpcPlayProtectVerdict.UNEVALUATED ->
                        SecurityIntegrityPlayProtectVerdict.UNEVALUATED
                }
            }
        )
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
            verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
        )
    }
}
