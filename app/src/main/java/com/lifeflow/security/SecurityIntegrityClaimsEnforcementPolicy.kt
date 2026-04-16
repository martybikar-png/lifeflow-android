package com.lifeflow.security

/**
 * Claim-based enforcement policy for already metadata-validated server verdicts.
 *
 * Purpose:
 * - keep structured claim decisions out of transport mapping
 * - decide whether VERIFIED may remain VERIFIED
 * - degrade or compromise when claims indicate weaker trust than the raw server verdict
 */
internal class SecurityIntegrityClaimsEnforcementPolicy {

    private enum class TargetVerdict {
        KEEP,
        DEGRADE,
        COMPROMISE
    }

    private data class EnforcementDecision(
        val target: TargetVerdict,
        val detail: String? = null
    )

    fun enforce(
        response: IntegrityTrustVerdictResponse
    ): IntegrityTrustVerdictResponse {
        if (response.verdictSource != IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER) {
            return response
        }

        val decision = evaluate(response)

        return when (decision.target) {
            TargetVerdict.KEEP -> response

            TargetVerdict.DEGRADE -> response.copy(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = buildString {
                    append(response.reason)
                    append(" | claimsPolicy=DEGRADED")
                    decision.detail?.let {
                        append(": ")
                        append(it)
                    }
                }
            )

            TargetVerdict.COMPROMISE -> response.copy(
                verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                reason = buildString {
                    append(response.reason)
                    append(" | claimsPolicy=COMPROMISED")
                    decision.detail?.let {
                        append(": ")
                        append(it)
                    }
                }
            )
        }
    }

    private fun evaluate(
        response: IntegrityTrustVerdictResponse
    ): EnforcementDecision {
        val claims = response.claims

        if (response.verdict == SecurityIntegrityTrustVerdict.COMPROMISED) {
            return EnforcementDecision(TargetVerdict.KEEP)
        }

        when (claims.appRecognitionVerdict) {
            SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION -> {
                return EnforcementDecision(
                    TargetVerdict.COMPROMISE,
                    "app recognition is UNRECOGNIZED_VERSION"
                )
            }

            else -> Unit
        }

        when (claims.appLicensingVerdict) {
            SecurityIntegrityAppLicensingVerdict.UNLICENSED -> {
                return EnforcementDecision(
                    TargetVerdict.COMPROMISE,
                    "app licensing is UNLICENSED"
                )
            }

            else -> Unit
        }

        when (claims.playProtectVerdict) {
            SecurityIntegrityPlayProtectVerdict.MEDIUM_RISK,
            SecurityIntegrityPlayProtectVerdict.HIGH_RISK -> {
                return EnforcementDecision(
                    TargetVerdict.COMPROMISE,
                    "Play Protect risk is ${claims.playProtectVerdict}"
                )
            }

            else -> Unit
        }

        if (response.verdict == SecurityIntegrityTrustVerdict.DEGRADED) {
            return EnforcementDecision(TargetVerdict.KEEP)
        }

        when (claims.appRecognitionVerdict) {
            SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED -> Unit

            SecurityIntegrityAppRecognitionVerdict.UNEVALUATED,
            null -> {
                return EnforcementDecision(
                    TargetVerdict.DEGRADE,
                    "app recognition is unevaluated"
                )
            }

            SecurityIntegrityAppRecognitionVerdict.UNRECOGNIZED_VERSION -> Unit
        }

        if (!hasTrustedPhysicalDeviceIntegrity(claims)) {
            return EnforcementDecision(
                TargetVerdict.DEGRADE,
                deviceIntegrityDetail(claims)
            )
        }

        when (claims.appLicensingVerdict) {
            SecurityIntegrityAppLicensingVerdict.LICENSED -> Unit

            SecurityIntegrityAppLicensingVerdict.UNEVALUATED,
            null -> {
                return EnforcementDecision(
                    TargetVerdict.DEGRADE,
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
                return EnforcementDecision(
                    TargetVerdict.DEGRADE,
                    "Play Protect verdict is not clean (${claims.playProtectVerdict ?: "null"})"
                )
            }

            SecurityIntegrityPlayProtectVerdict.MEDIUM_RISK,
            SecurityIntegrityPlayProtectVerdict.HIGH_RISK -> Unit
        }

        return EnforcementDecision(TargetVerdict.KEEP)
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
}
