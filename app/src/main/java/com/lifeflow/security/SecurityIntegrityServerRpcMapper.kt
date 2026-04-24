package com.lifeflow.security

/**
 * Maps server RPC integrity verdict payloads into the app-level trust model.
 */
internal class SecurityIntegrityServerRpcMapper {
    fun map(
        response: IntegrityTrustRpcResponse
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = mapVerdict(response.verdict),
            reason = response.reason.trim(),
            requestHashEcho = response.requestHashEcho,
            requestBindingVerified = response.requestBindingVerified,
            serverTimestampEpochMs = response.serverTimestampEpochMs,
            policyVersion = response.policyVersion,
            verdictSource = mapSource(response.verdictSource),
            claims = mapClaims(response.claims),
            attestationVerification = mapAttestationVerification(
                response.attestationVerification
            ),
            decision = mapDecision(response.decision),
            decisionReasonCode = response.decisionReasonCode
        )
    }

    private fun mapVerdict(
        verdict: IntegrityTrustRpcVerdict
    ): SecurityIntegrityTrustVerdict {
        return when (verdict) {
            IntegrityTrustRpcVerdict.VERIFIED ->
                SecurityIntegrityTrustVerdict.VERIFIED

            IntegrityTrustRpcVerdict.DEGRADED ->
                SecurityIntegrityTrustVerdict.DEGRADED

            IntegrityTrustRpcVerdict.COMPROMISED ->
                SecurityIntegrityTrustVerdict.COMPROMISED
        }
    }

    private fun mapSource(
        source: IntegrityTrustRpcVerdictSource
    ): IntegrityTrustVerdictSource {
        return when (source) {
            IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
                IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
        }
    }

    private fun mapDecision(
        decision: IntegrityTrustRpcDecision
    ): IntegrityTrustDecision {
        return when (decision) {
            IntegrityTrustRpcDecision.ALLOW -> IntegrityTrustDecision.ALLOW
            IntegrityTrustRpcDecision.STEP_UP -> IntegrityTrustDecision.STEP_UP
            IntegrityTrustRpcDecision.DEGRADED -> IntegrityTrustDecision.DEGRADED
            IntegrityTrustRpcDecision.DENY -> IntegrityTrustDecision.DENY
            IntegrityTrustRpcDecision.LOCK -> IntegrityTrustDecision.LOCK
        }
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
            deviceRecognitionVerdicts = claims.deviceRecognitionVerdicts.mapTo(
                linkedSetOf()
            ) { rpcValue ->
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
}