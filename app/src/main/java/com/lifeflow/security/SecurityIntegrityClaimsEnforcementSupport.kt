package com.lifeflow.security

internal enum class SecurityIntegrityClaimsTargetVerdict {
    KEEP,
    DEGRADE,
    COMPROMISE
}

internal data class SecurityIntegrityClaimsEnforcementDecision(
    val target: SecurityIntegrityClaimsTargetVerdict,
    val detail: String? = null
)

internal fun evaluateSecurityIntegrityClaims(
    response: IntegrityTrustVerdictResponse
): SecurityIntegrityClaimsEnforcementDecision {
    val claims = response.claims

    if (response.verdict == SecurityIntegrityTrustVerdict.COMPROMISED) {
        return SecurityIntegrityClaimsEnforcementDecision(
            SecurityIntegrityClaimsTargetVerdict.KEEP
        )
    }

    when (claims.appRecognitionVerdict) {
        SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.COMPROMISE,
                "app recognition is UNRECOGNIZED_VERSION"
            )
        }

        else -> Unit
    }

    when (claims.appLicensingVerdict) {
        SecurityIntegrityAppLicensingVerdict.UNLICENSED -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.COMPROMISE,
                "app licensing is UNLICENSED"
            )
        }

        else -> Unit
    }

    when (claims.playProtectVerdict) {
        SecurityIntegrityPlayProtectVerdict.MEDIUM_RISK,
        SecurityIntegrityPlayProtectVerdict.HIGH_RISK -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.COMPROMISE,
                "Play Protect risk is ${claims.playProtectVerdict}"
            )
        }

        else -> Unit
    }

    if (response.verdict == SecurityIntegrityTrustVerdict.DEGRADED) {
        return SecurityIntegrityClaimsEnforcementDecision(
            SecurityIntegrityClaimsTargetVerdict.KEEP
        )
    }

    when (claims.appRecognitionVerdict) {
        SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED -> Unit

        SecurityIntegrityAppRecognitionVerdict.UNEVALUATED,
        null -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.DEGRADE,
                "app recognition is unevaluated"
            )
        }

        SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION -> Unit
    }

    if (!hasTrustedPhysicalDeviceIntegrity(claims)) {
        return SecurityIntegrityClaimsEnforcementDecision(
            SecurityIntegrityClaimsTargetVerdict.DEGRADE,
            deviceIntegrityDetail(claims)
        )
    }

    when (claims.appLicensingVerdict) {
        SecurityIntegrityAppLicensingVerdict.LICENSED -> Unit

        SecurityIntegrityAppLicensingVerdict.UNEVALUATED,
        null -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.DEGRADE,
                "app licensing is unevaluated"
            )
        }

        SecurityIntegrityAppLicensingVerdict.UNLICENSED -> Unit
    }

    when (claims.playProtectVerdict) {
        SecurityIntegrityPlayProtectVerdict.NO_ISSUES -> Unit

        SecurityIntegrityPlayProtectVerdict.NO_DATA,
        SecurityIntegrityPlayProtectVerdict.POSSIBLE_RISK,
        SecurityIntegrityPlayProtectVerdict.UNEVALUATED,
        null -> {
            return SecurityIntegrityClaimsEnforcementDecision(
                SecurityIntegrityClaimsTargetVerdict.DEGRADE,
                "Play Protect verdict is not clean (${claims.playProtectVerdict ?: "null"})"
            )
        }

        SecurityIntegrityPlayProtectVerdict.MEDIUM_RISK,
        SecurityIntegrityPlayProtectVerdict.HIGH_RISK -> Unit
    }

    return SecurityIntegrityClaimsEnforcementDecision(
        SecurityIntegrityClaimsTargetVerdict.KEEP
    )
}

private fun hasTrustedPhysicalDeviceIntegrity(
    claims: SecurityIntegrityVerdictClaims
): Boolean {
    return claims.deviceRecognitionVerdicts.contains(
        SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
    ) || claims.deviceRecognitionVerdicts.contains(
        SecurityIntegrityDeviceRecognitionVerdict.MEETS_STRONG_INTEGRITY
    )
}

private fun deviceIntegrityDetail(
    claims: SecurityIntegrityVerdictClaims
): String {
    return when {
        claims.deviceRecognitionVerdicts.isEmpty() ->
            "device integrity claims are missing"

        claims.deviceRecognitionVerdicts.contains(
            SecurityIntegrityDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY
        ) ->
            "device integrity is basic only"

        claims.deviceRecognitionVerdicts.contains(
            SecurityIntegrityDeviceRecognitionVerdict.MEETS_VIRTUAL_INTEGRITY
        ) ->
            "device integrity is virtual only"

        else ->
            "device integrity is insufficient (${claims.deviceRecognitionVerdicts.joinToString()})"
    }
}
