package com.lifeflow.security

/**
 * Backend/RPC-side contract for integrity trust verdict exchange.
 *
 * Purpose:
 * - isolate future generated gRPC/proto models from internal runtime models
 * - keep transport/client wiring independent from domain-facing verdict models
 */
internal enum class IntegrityTrustRpcVerdict {
    VERIFIED,
    DEGRADED,
    COMPROMISED
}

internal enum class IntegrityTrustRpcVerdictSource {
    PLAY_INTEGRITY_STANDARD_SERVER
}

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

internal data class IntegrityTrustRpcRequest(
    val requestHash: String,
    val integrityToken: String
) {
    init {
        require(requestHash.isNotBlank()) { "RPC requestHash must not be blank." }
        require(integrityToken.isNotBlank()) { "RPC integrityToken must not be blank." }
    }
}

internal data class IntegrityTrustRpcResponse(
    val verdict: IntegrityTrustRpcVerdict,
    val reason: String,
    val requestHashEcho: String,
    val serverTimestampEpochMs: Long,
    val policyVersion: String,
    val verdictSource: IntegrityTrustRpcVerdictSource,
    val claims: IntegrityTrustRpcClaims = IntegrityTrustRpcClaims()
) {
    init {
        require(reason.isNotBlank()) { "RPC reason must not be blank." }
        require(requestHashEcho.isNotBlank()) { "RPC requestHashEcho must not be blank." }
        require(serverTimestampEpochMs > 0L) { "RPC serverTimestampEpochMs must be > 0." }
        require(policyVersion.isNotBlank()) { "RPC policyVersion must not be blank." }
    }
}
