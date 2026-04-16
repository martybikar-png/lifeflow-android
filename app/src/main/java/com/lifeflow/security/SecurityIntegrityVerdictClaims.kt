package com.lifeflow.security

/**
 * Internal structured claim profile derived from a verified server verdict.
 *
 * Purpose:
 * - keep server claim semantics explicit and typed
 * - carry audit-relevant integrity signals deeper into the app
 * - prepare future stricter enforcement without reshaping verdict transport
 */
internal enum class SecurityIntegrityAppRecognitionVerdict {
    PLAY_RECOGNIZED,
    UNRECOGNIZED_VERSION,
    UNEVALUATED
}

internal enum class SecurityIntegrityDeviceRecognitionVerdict {
    MEETS_BASIC_INTEGRITY,
    MEETS_DEVICE_INTEGRITY,
    MEETS_STRONG_INTEGRITY,
    MEETS_VIRTUAL_INTEGRITY
}

internal enum class SecurityIntegrityAppLicensingVerdict {
    LICENSED,
    UNLICENSED,
    UNEVALUATED
}

internal enum class SecurityIntegrityPlayProtectVerdict {
    NO_ISSUES,
    NO_DATA,
    POSSIBLE_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    UNEVALUATED
}

internal data class SecurityIntegrityVerdictClaims(
    val appRecognitionVerdict: SecurityIntegrityAppRecognitionVerdict? = null,
    val deviceRecognitionVerdicts: Set<SecurityIntegrityDeviceRecognitionVerdict> = emptySet(),
    val appLicensingVerdict: SecurityIntegrityAppLicensingVerdict? = null,
    val playProtectVerdict: SecurityIntegrityPlayProtectVerdict? = null
)
