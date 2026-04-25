package com.lifeflow.security

internal enum class IntegrityTrustRpcAppRecognitionVerdict {
    PLAY_RECOGNIZED,
    UNRECOGNIZED_VERSION,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcDeviceRecognitionVerdict {
    MEETS_BASIC_INTEGRITY,
    MEETS_DEVICE_INTEGRITY,
    MEETS_STRONG_INTEGRITY,
    MEETS_VIRTUAL_INTEGRITY
}

internal enum class IntegrityTrustRpcAppLicensingVerdict {
    LICENSED,
    UNLICENSED,
    UNEVALUATED
}

internal enum class IntegrityTrustRpcPlayProtectVerdict {
    NO_ISSUES,
    NO_DATA,
    POSSIBLE_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    UNEVALUATED
}

internal data class IntegrityTrustRpcClaims(
    val appRecognitionVerdict: IntegrityTrustRpcAppRecognitionVerdict? = null,
    val deviceRecognitionVerdicts: Set<IntegrityTrustRpcDeviceRecognitionVerdict> = emptySet(),
    val appLicensingVerdict: IntegrityTrustRpcAppLicensingVerdict? = null,
    val playProtectVerdict: IntegrityTrustRpcPlayProtectVerdict? = null
)
