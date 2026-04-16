package com.lifeflow.security

import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import java.util.concurrent.TimeUnit

/**
 * gRPC transport scaffold for the external integrity trust verdict authority.
 *
 * Important:
 * - transport owns secure channel construction only
 * - verdict RPC behavior lives in dedicated control client
 * - secure channel bootstrap installs the updated Android security provider first
 * - endpoint and channel policy values come from IntegrityTrustTransportConfig
 * - transport remains fail-closed until the real RPC contract and mTLS material are wired
 */
internal class GrpcIntegrityTrustTransport private constructor(
    private val applicationContext: Context,
    private val config: IntegrityTrustTransportConfig
) : IntegrityTrustTransport {

    companion object {
        fun create(
            applicationContext: Context
        ): GrpcIntegrityTrustTransport {
            return GrpcIntegrityTrustTransport(
                applicationContext = applicationContext.applicationContext,
                config = IntegrityTrustTransportConfig.fromBuildConfig()
            )
        }
    }

    private val verdictChannelDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        buildSecureChannel(config.verdictEndpoint)
    }

    private val verdictChannel: ManagedChannel by verdictChannelDelegate

    private val controlClient: IntegrityTrustControlClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GrpcIntegrityTrustControlClient(verdictChannel)
    }

    override fun requestVerdict(
        request: IntegrityTrustVerdictRequest
    ): IntegrityTrustVerdictResponse {
        return controlClient.requestVerdict(request)
    }

    override fun close() {
        if (verdictChannelDelegate.isInitialized()) {
            verdictChannel.shutdown()
        }
    }

    fun shutdownNow() {
        if (verdictChannelDelegate.isInitialized()) {
            verdictChannel.shutdownNow()
        }
    }

    fun awaitTermination(
        timeoutMs: Long
    ): Boolean {
        require(timeoutMs > 0L) { "timeoutMs must be > 0." }

        if (!verdictChannelDelegate.isInitialized()) {
            return true
        }

        return verdictChannel.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)
    }

    private fun buildSecureChannel(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): ManagedChannel {
        ensureUpdatedSecurityProvider()
        validateSecureEndpoint(endpoint)

        val builder = OkHttpChannelBuilder
            .forAddress(endpoint.host, endpoint.port)
            .useTransportSecurity()

        applyChannelPolicy(
            builder = builder,
            channelPolicy = endpoint.channelPolicy
        )

        return builder.build()
    }

    private fun validateSecureEndpoint(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ) {
        val host = endpoint.host.trim()
        val policy = endpoint.securityPolicy

        require("://" !in host) {
            "Integrity trust host must not include a URI scheme: $host"
        }
        require('/' !in host && '?' !in host && '#' !in host) {
            "Integrity trust host must not include URI path/query/fragment: $host"
        }

        if (!policy.allowPlaceholderHost && host.endsWith(".invalid", ignoreCase = true)) {
            throw SecurityException(
                "Integrity trust host remains placeholder/unconfigured: $host"
            )
        }

        if (!policy.allowLoopbackHost && isLoopbackHost(host)) {
            throw SecurityException(
                "Integrity trust loopback host is not allowed for secure transport: $host"
            )
        }

        if (!policy.allowIpLiteralHost && isIpLiteralHost(host)) {
            throw SecurityException(
                "Integrity trust IP-literal host is not allowed for secure transport: $host"
            )
        }
    }

    private fun isLoopbackHost(host: String): Boolean {
        return host.equals("localhost", ignoreCase = true) ||
            host.equals("ip6-localhost", ignoreCase = true) ||
            host == "127.0.0.1" ||
            host == "::1" ||
            host == "[::1]"
    }

    private fun isIpLiteralHost(host: String): Boolean {
        val ipv4Pattern = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")
        if (ipv4Pattern.matches(host)) {
            return true
        }

        val bracketedIpv6Pattern = Regex("""^\[[0-9a-fA-F:]+]$""")
        if (bracketedIpv6Pattern.matches(host)) {
            return true
        }

        val bareIpv6Pattern = Regex("""^[0-9a-fA-F:]+$""")
        return ':' in host && bareIpv6Pattern.matches(host)
    }

    private fun applyChannelPolicy(
        builder: OkHttpChannelBuilder,
        channelPolicy: IntegrityTrustTransportConfig.ChannelPolicy
    ) {
        if (!channelPolicy.keepAliveEnabled) {
            return
        }

        builder.keepAliveTime(
            channelPolicy.keepAliveTimeMs,
            TimeUnit.MILLISECONDS
        )
        builder.keepAliveTimeout(
            channelPolicy.keepAliveTimeoutMs,
            TimeUnit.MILLISECONDS
        )
        builder.keepAliveWithoutCalls(
            channelPolicy.keepAliveWithoutCalls
        )
    }

    private fun ensureUpdatedSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(applicationContext)
        } catch (e: GooglePlayServicesRepairableException) {
            throw SecurityException(
                "Updated Android security provider is required before secure integrity trust communication.",
                e
            )
        } catch (e: GooglePlayServicesNotAvailableException) {
            throw SecurityException(
                "Android security provider could not be updated for secure integrity trust communication.",
                e
            )
        }
    }
}
