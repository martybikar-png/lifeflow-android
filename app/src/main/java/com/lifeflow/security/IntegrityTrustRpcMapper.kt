package com.lifeflow.security

/**
 * Mapping layer between internal integrity trust models and backend/RPC contract.
 *
 * Rules:
 * - request mapping is structural
 * - response mapping is structural only
 * - verdict semantics / fail-closed policy live in SecurityIntegrityTrustAuthority
 *   via SecurityIntegrityServerVerdictPolicy
 */
internal object IntegrityTrustRpcMapper {

    fun toRpcRequest(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustRpcRequest {
        return IntegrityTrustRpcRequest(
            requestHash = request.requestHash,
            integrityToken = request.integrityToken
        )
    }

    fun fromRpcResponse(
        response: IntegrityTrustRpcResponse,
        expectedRequestHash: String
    ): IntegrityTrustVerdictResponse {
        require(expectedRequestHash.isNotBlank()) {
            "expectedRequestHash must not be blank."
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
            )
        )
    }
}
