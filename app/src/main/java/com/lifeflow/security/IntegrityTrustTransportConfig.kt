package com.lifeflow.security

import com.lifeflow.BuildConfig

/**
 * External integrity trust transport configuration.
 *
 * Purpose:
 * - keep endpoint values out of transport logic
 * - keep channel policy out of transport logic
 * - allow future mTLS material and routing policy to evolve without reshaping transport code
 */
internal data class IntegrityTrustTransportConfig(
    val verdictEndpoint: EndpointConfig
) {

    data class EndpointConfig(
        val host: String,
        val port: Int,
        val channelPolicy: ChannelPolicy,
        val securityPolicy: SecurityPolicy = SecurityPolicy()
    ) {
        init {
            require(host.isNotBlank()) { "Integrity trust host must not be blank." }
            require(port in 1..65535) { "Integrity trust port must be in range 1..65535." }
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
        fun fromBuildConfig(): IntegrityTrustTransportConfig {
            val strictSecurityPolicy = SecurityPolicy()

            return IntegrityTrustTransportConfig(
                verdictEndpoint = EndpointConfig(
                    host = BuildConfig.INTEGRITY_TRUST_VERDICT_HOST,
                    port = BuildConfig.INTEGRITY_TRUST_VERDICT_PORT,
                    channelPolicy = ChannelPolicy(
                        keepAliveEnabled = BuildConfig.INTEGRITY_TRUST_VERDICT_KEEPALIVE_ENABLED,
                        keepAliveTimeMs = BuildConfig.INTEGRITY_TRUST_VERDICT_KEEPALIVE_TIME_MS,
                        keepAliveTimeoutMs = BuildConfig.INTEGRITY_TRUST_VERDICT_KEEPALIVE_TIMEOUT_MS,
                        keepAliveWithoutCalls = BuildConfig.INTEGRITY_TRUST_VERDICT_KEEPALIVE_WITHOUT_CALLS
                    ),
                    securityPolicy = strictSecurityPolicy
                )
            )
        }
    }
}
