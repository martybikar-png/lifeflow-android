package com.lifeflow.security

internal fun mapSecurityIntegrityVerdictClaimsFromRpc(
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
        deviceRecognitionVerdicts =
            claims.deviceRecognitionVerdicts.mapTo(linkedSetOf()) { rpcValue ->
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
