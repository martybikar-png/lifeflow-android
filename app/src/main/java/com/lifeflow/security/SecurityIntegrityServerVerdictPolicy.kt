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

                val responseWithAttestationPolicy = enforceAttestationVerification(
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
                requestBindingVerified = response.requestBindingVerified,
                serverTimestampEpochMs = response.serverTimestampEpochMs,
                policyVersion = response.policyVersion,
                verdictSource = mappedSource,
                claims = mapClaims(response.claims),
                attestationVerification = mapAttestationVerification(
                    response.attestationVerification
                ),
                decision = when (response.decision) {
                    IntegrityTrustRpcDecision.ALLOW -> IntegrityTrustDecision.ALLOW
                    IntegrityTrustRpcDecision.STEP_UP -> IntegrityTrustDecision.STEP_UP
                    IntegrityTrustRpcDecision.DEGRADED -> IntegrityTrustDecision.DEGRADED
                    IntegrityTrustRpcDecision.DENY -> IntegrityTrustDecision.DENY
                    IntegrityTrustRpcDecision.LOCK -> IntegrityTrustDecision.LOCK
                },
                decisionReasonCode = response.decisionReasonCode
            ),
            nowEpochMs = System.currentTimeMillis()
        )
    }

    private fun enforceAttestationVerification(
        response: IntegrityTrustVerdictResponse
    ): IntegrityTrustVerdictResponse {
        val verification = response.attestationVerification
            ?: return attestationFailClosed(
                detail = "missing attestationVerification",
                verification = null
            )

        if (verification.challengeVerdict !=
            IntegrityTrustAttestationChallengeVerdict.MATCHED
        ) {
            return attestationFailClosed(
                detail = "challenge verdict is ${verification.challengeVerdict}",
                verification = verification
            )
        }

        if (verification.chainVerdict !=
            IntegrityTrustAttestationChainVerdict.VERIFIED
        ) {
            return attestationFailClosed(
                detail = "chain verdict is ${verification.chainVerdict}",
                verification = verification
            )
        }

        if (verification.rootVerdict !=
            IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED
        ) {
            return attestationFailClosed(
                detail = "root verdict is ${verification.rootVerdict}",
                verification = verification
            )
        }

        if (verification.revocationVerdict !=
            IntegrityTrustAttestationRevocationVerdict.CLEAN
        ) {
            return attestationFailClosed(
                detail = "revocation verdict is ${verification.revocationVerdict}",
                verification = verification
            )
        }

        if (verification.appBindingVerdict !=
            IntegrityTrustAttestationAppBindingVerdict.MATCHED
        ) {
            return attestationFailClosed(
                detail = "app binding verdict is ${verification.appBindingVerdict}",
                verification = verification
            )
        }

        return response.copy(
            reason = buildString {
                append(response.reason)
                append(" | attestationChain=")
                append(verification.chainVerdict)
                append(" | attestationChallenge=")
                append(verification.challengeVerdict)
                append(" | attestationRoot=")
                append(verification.rootVerdict)
                append(" | attestationRevocation=")
                append(verification.revocationVerdict)
                append(" | attestationAppBinding=")
                append(verification.appBindingVerdict)
                verification.detail?.let {
                    append(" | attestationDetail=")
                    append(it)
                }
            }
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

    private fun mapAttestationVerification(
        verification: IntegrityTrustRpcAttestationVerification?
    ): IntegrityTrustAttestationVerification? {
        return verification?.let {
            IntegrityTrustAttestationVerification(
                chainVerdict = when (it.chainVerdict) {
                    IntegrityTrustRpcAttestationChainVerdict.VERIFIED ->
                        IntegrityTrustAttestationChainVerdict.VERIFIED

                    IntegrityTrustRpcAttestationChainVerdict.FAILED ->
                        IntegrityTrustAttestationChainVerdict.FAILED

                    IntegrityTrustRpcAttestationChainVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationChainVerdict.UNEVALUATED
                },
                challengeVerdict = when (it.challengeVerdict) {
                    IntegrityTrustRpcAttestationChallengeVerdict.MATCHED ->
                        IntegrityTrustAttestationChallengeVerdict.MATCHED

                    IntegrityTrustRpcAttestationChallengeVerdict.MISMATCHED ->
                        IntegrityTrustAttestationChallengeVerdict.MISMATCHED

                    IntegrityTrustRpcAttestationChallengeVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationChallengeVerdict.UNEVALUATED
                },
                rootVerdict = when (it.rootVerdict) {
                    IntegrityTrustRpcAttestationRootVerdict.GOOGLE_TRUSTED ->
                        IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED

                    IntegrityTrustRpcAttestationRootVerdict.UNTRUSTED ->
                        IntegrityTrustAttestationRootVerdict.UNTRUSTED

                    IntegrityTrustRpcAttestationRootVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationRootVerdict.UNEVALUATED
                },
                revocationVerdict = when (it.revocationVerdict) {
                    IntegrityTrustRpcAttestationRevocationVerdict.CLEAN ->
                        IntegrityTrustAttestationRevocationVerdict.CLEAN

                    IntegrityTrustRpcAttestationRevocationVerdict.REVOKED ->
                        IntegrityTrustAttestationRevocationVerdict.REVOKED

                    IntegrityTrustRpcAttestationRevocationVerdict.UNCHECKED ->
                        IntegrityTrustAttestationRevocationVerdict.UNCHECKED

                    IntegrityTrustRpcAttestationRevocationVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationRevocationVerdict.UNEVALUATED
                },
                appBindingVerdict = when (it.appBindingVerdict) {
                    IntegrityTrustRpcAttestationAppBindingVerdict.MATCHED ->
                        IntegrityTrustAttestationAppBindingVerdict.MATCHED

                    IntegrityTrustRpcAttestationAppBindingVerdict.MISMATCHED ->
                        IntegrityTrustAttestationAppBindingVerdict.MISMATCHED

                    IntegrityTrustRpcAttestationAppBindingVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationAppBindingVerdict.UNEVALUATED
                },
                detail = it.detail
            )
        }
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

    private fun attestationFailClosed(
        detail: String,
        verification: IntegrityTrustAttestationVerification?
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "SERVER_VERDICT_ATTESTATION_INVALID: $detail",
            verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            attestationVerification = verification,
            decision = IntegrityTrustDecision.LOCK,
            decisionReasonCode = "SERVER_ATTESTATION_INVALID"
        )
    }
}
