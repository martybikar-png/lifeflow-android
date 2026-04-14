package com.lifeflow.security

import com.lifeflow.BuildConfig

/**
 * External emergency authority transport configuration.
 *
 * Purpose:
 * - keep endpoint values out of transport logic
 * - keep channel policy out of transport logic
 * - allow future mTLS material and routing policy to evolve without reshaping transport code
 */
internal data class EmergencyAuthorityTransportConfig(
    val controlEndpoint: EndpointConfig,
    val auditEndpoint: EndpointConfig
) {

    data class EndpointConfig(
        val host: String,
        val port: Int,
        val channelPolicy: ChannelPolicy,
        val securityPolicy: SecurityPolicy = SecurityPolicy()
    ) {
        init {
            require(host.isNotBlank()) { "Emergency authority host must not be blank." }
            require(port in 1..65535) { "Emergency authority port must be in range 1..65535." }
        }
    }

    data class ChannelPolicy(
        val keepAliveEnabled: Boolean,
        val keepAliveTimeMs: Long,
        val keepAliveTimeoutMs: Long,
        val keepAliveWithoutCalls: Boolean
    ) {
        init {
            require(keepAliveTimeMs > 0L) { "keepAliveTimeMs must be > 0." }
            require(keepAliveTimeoutMs > 0L) { "keepAliveTimeoutMs must be > 0." }
        }
    }

    data class SecurityPolicy(
        val allowCustomHostnameVerifier: Boolean = false,
        val allowLoopbackHost: Boolean = false,
        val allowIpLiteralHost: Boolean = false,
        val allowPlaceholderHost: Boolean = false
    )

    companion object {
        fun fromBuildConfig(): EmergencyAuthorityTransportConfig {
            val strictSecurityPolicy = SecurityPolicy()

            return EmergencyAuthorityTransportConfig(
                controlEndpoint = EndpointConfig(
                    host = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_HOST,
                    port = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_PORT,
                    channelPolicy = ChannelPolicy(
                        keepAliveEnabled = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_ENABLED,
                        keepAliveTimeMs = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIME_MS,
                        keepAliveTimeoutMs = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIMEOUT_MS,
                        keepAliveWithoutCalls = BuildConfig.EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_WITHOUT_CALLS
                    ),
                    securityPolicy = strictSecurityPolicy
                ),
                auditEndpoint = EndpointConfig(
                    host = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_HOST,
                    port = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_PORT,
                    channelPolicy = ChannelPolicy(
                        keepAliveEnabled = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_ENABLED,
                        keepAliveTimeMs = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_TIME_MS,
                        keepAliveTimeoutMs = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_TIMEOUT_MS,
                        keepAliveWithoutCalls = BuildConfig.EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_WITHOUT_CALLS
                    ),
                    securityPolicy = strictSecurityPolicy
                )
            )
        }
    }
}
