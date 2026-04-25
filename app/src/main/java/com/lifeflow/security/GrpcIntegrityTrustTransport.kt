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
 * - endpoint and TLS policy validation live in GrpcIntegrityTrustEndpointValidator
 * - mTLS material comes from IntegrityTrustTlsMaterialProvider
 * - transport remains fail-closed until the real RPC contract and mTLS material are wired
 */
internal class GrpcIntegrityTrustTransport private constructor(
    private val applicationContext: Context,
    private val config: IntegrityTrustTransportConfig,
    private val tlsMaterialProvider: IntegrityTrustTlsMaterialProvider
) : IntegrityTrustTransport {

    companion object {
        fun create(
            applicationContext: Context,
            tlsMaterialProvider: IntegrityTrustTlsMaterialProvider
        ): GrpcIntegrityTrustTransport {
            return GrpcIntegrityTrustTransport(
                applicationContext = applicationContext.applicationContext,
                config = IntegrityTrustTransportConfig.fromBuildConfig(),
                tlsMaterialProvider = tlsMaterialProvider
            )
        }
    }

    private val endpointValidator = GrpcIntegrityTrustEndpointValidator()

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
        endpointValidator.validateSecureEndpoint(endpoint)

        val tlsMaterial = tlsMaterialProvider.materialFor(endpoint)
        endpointValidator.validateTlsMaterialPolicy(endpoint, tlsMaterial)

        val builder = OkHttpChannelBuilder
            .forAddress(endpoint.host, endpoint.port)
            .useTransportSecurity()
            .sslSocketFactory(tlsMaterial.sslSocketFactory)
            .hostnameVerifier(endpointValidator.exactHostnameVerifier(endpoint.host))

        tlsMaterial.hostnameVerifier?.let { hostnameVerifier ->
            builder.hostnameVerifier(hostnameVerifier)
        }

        applyChannelPolicy(
            builder = builder,
            channelPolicy = endpoint.channelPolicy
        )

        return builder.build()
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