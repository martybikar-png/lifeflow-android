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
            attestationEvidence = request.attestationEvidence?.let { evidence ->
                IntegrityTrustRpcAttestationEvidence(
                    status = when (evidence.status) {
                        IntegrityTrustAttestationStatus.CAPTURED ->
                            IntegrityTrustRpcAttestationStatus.CAPTURED

                        IntegrityTrustAttestationStatus.UNAVAILABLE ->
                            IntegrityTrustRpcAttestationStatus.UNAVAILABLE
                    },
                    keyAlias = evidence.keyAlias,
                    chainEntryCount = evidence.chainEntryCount,
                    challengeBase64 = evidence.challengeBase64,
                    certificateChainDerBase64 = evidence.certificateChainDerBase64,
                    challengeSha256 = evidence.challengeSha256,
                    leafCertificateSha256 = evidence.leafCertificateSha256,
                    strongBoxRequested = evidence.strongBoxRequested,
                    failureKind = evidence.failureKind?.let {
                        when (it) {
                            IntegrityTrustAttestationFailureKind.UNAVAILABLE ->
                                IntegrityTrustRpcAttestationFailureKind.UNAVAILABLE

                            IntegrityTrustAttestationFailureKind.HARD_FAILURE ->
                                IntegrityTrustRpcAttestationFailureKind.HARD_FAILURE
                        }
                    },
                    failureReason = evidence.failureReason
                )
            }
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
            verdict = when (response.verdict) {
                IntegrityTrustRpcVerdict.VERIFIED ->
                    SecurityIntegrityTrustVerdict.VERIFIED

                IntegrityTrustRpcVerdict.DEGRADED ->
                    SecurityIntegrityTrustVerdict.DEGRADED

                IntegrityTrustRpcVerdict.COMPROMISED ->
                    SecurityIntegrityTrustVerdict.COMPROMISED
            },
            reason = response.reason.trim(),
            requestHashEcho = response.requestHashEcho,
            requestBindingVerified = response.requestBindingVerified,
            serverTimestampEpochMs = response.serverTimestampEpochMs,
            policyVersion = response.policyVersion,
            verdictSource = when (response.verdictSource) {
                IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
                    IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
            },
            claims = SecurityIntegrityVerdictClaims(
                appRecognitionVerdict = response.claims.appRecognitionVerdict?.let { rpcValue ->
                    when (rpcValue) {
                        IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED ->
                            SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED

                        IntegrityTrustRpcAppRecognitionVerdict.UNRECOGNIZED_VERSION ->
                            SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION

                        IntegrityTrustRpcAppRecognitionVerdict.UNEVALUATED ->
                            SecurityIntegrityAppRecognitionVerdict.UNEVALUATED
                    }
                },
                deviceRecognitionVerdicts =
                    response.claims.deviceRecognitionVerdicts.mapTo(linkedSetOf()) { rpcValue ->
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
                appLicensingVerdict = response.claims.appLicensingVerdict?.let { rpcValue ->
                    when (rpcValue) {
                        IntegrityTrustRpcAppLicensingVerdict.LICENSED ->
                            SecurityIntegrityAppLicensingVerdict.LICENSED

                        IntegrityTrustRpcAppLicensingVerdict.UNLICENSED ->
                            SecurityIntegrityAppLicensingVerdict.UNLICENSED

                        IntegrityTrustRpcAppLicensingVerdict.UNEVALUATED ->
                            SecurityIntegrityAppLicensingVerdict.UNEVALUATED
                    }
                },
                playProtectVerdict = response.claims.playProtectVerdict?.let { rpcValue ->
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
            ),
            attestationVerification = response.attestationVerification?.let { verification ->
                IntegrityTrustAttestationVerification(
                    chainVerdict = when (verification.chainVerdict) {
                        IntegrityTrustRpcAttestationChainVerdict.VERIFIED ->
                            IntegrityTrustAttestationChainVerdict.VERIFIED

                        IntegrityTrustRpcAttestationChainVerdict.FAILED ->
                            IntegrityTrustAttestationChainVerdict.FAILED

                        IntegrityTrustRpcAttestationChainVerdict.UNEVALUATED ->
                            IntegrityTrustAttestationChainVerdict.UNEVALUATED
                    },
                    challengeVerdict = when (verification.challengeVerdict) {
                        IntegrityTrustRpcAttestationChallengeVerdict.MATCHED ->
                            IntegrityTrustAttestationChallengeVerdict.MATCHED

                        IntegrityTrustRpcAttestationChallengeVerdict.MISMATCHED ->
                            IntegrityTrustAttestationChallengeVerdict.MISMATCHED

                        IntegrityTrustRpcAttestationChallengeVerdict.UNEVALUATED ->
                            IntegrityTrustAttestationChallengeVerdict.UNEVALUATED
                    },
                    rootVerdict = when (verification.rootVerdict) {
                        IntegrityTrustRpcAttestationRootVerdict.GOOGLE_TRUSTED ->
                            IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED

                        IntegrityTrustRpcAttestationRootVerdict.UNTRUSTED ->
                            IntegrityTrustAttestationRootVerdict.UNTRUSTED

                        IntegrityTrustRpcAttestationRootVerdict.UNEVALUATED ->
                            IntegrityTrustAttestationRootVerdict.UNEVALUATED
                    },
                    revocationVerdict = when (verification.revocationVerdict) {
                        IntegrityTrustRpcAttestationRevocationVerdict.CLEAN ->
                            IntegrityTrustAttestationRevocationVerdict.CLEAN

                        IntegrityTrustRpcAttestationRevocationVerdict.REVOKED ->
                            IntegrityTrustAttestationRevocationVerdict.REVOKED

                        IntegrityTrustRpcAttestationRevocationVerdict.UNCHECKED ->
                            IntegrityTrustAttestationRevocationVerdict.UNCHECKED

                        IntegrityTrustRpcAttestationRevocationVerdict.UNEVALUATED ->
                            IntegrityTrustAttestationRevocationVerdict.UNEVALUATED
                    },
                    appBindingVerdict = when (verification.appBindingVerdict) {
                        IntegrityTrustRpcAttestationAppBindingVerdict.MATCHED ->
                            IntegrityTrustAttestationAppBindingVerdict.MATCHED

                        IntegrityTrustRpcAttestationAppBindingVerdict.MISMATCHED ->
                            IntegrityTrustAttestationAppBindingVerdict.MISMATCHED

                        IntegrityTrustRpcAttestationAppBindingVerdict.UNEVALUATED ->
                            IntegrityTrustAttestationAppBindingVerdict.UNEVALUATED
                    },
                    detail = verification.detail
                )
            },
            decision = when (response.decision) {
                IntegrityTrustRpcDecision.ALLOW ->
                    IntegrityTrustDecision.ALLOW

                IntegrityTrustRpcDecision.STEP_UP ->
                    IntegrityTrustDecision.STEP_UP

                IntegrityTrustRpcDecision.DEGRADED ->
                    IntegrityTrustDecision.DEGRADED

                IntegrityTrustRpcDecision.DENY ->
                    IntegrityTrustDecision.DENY

                IntegrityTrustRpcDecision.LOCK ->
                    IntegrityTrustDecision.LOCK
            },
            decisionReasonCode = response.decisionReasonCode
        )
    }
}
