package com.lifeflow.security

import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import java.util.concurrent.TimeUnit

internal class GrpcEmergencyAuthoritySecureChannelFactory(
    private val applicationContext: Context,
    private val tlsMaterialProvider: EmergencyAuthorityTlsMaterialProvider
) {
    private val endpointValidator = GrpcEmergencyAuthorityEndpointValidator()

    fun buildSecureChannel(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig
    ): ManagedChannel {
        ensureUpdatedSecurityProvider()
        endpointValidator.validateSecureEndpoint(endpoint)

        val tlsMaterial = tlsMaterialProvider.materialFor(endpoint)
        endpointValidator.validateTlsMaterialPolicy(
            endpoint = endpoint,
            tlsMaterial = tlsMaterial
        )

        val builder = OkHttpChannelBuilder
            .forAddress(endpoint.host, endpoint.port)
            .useTransportSecurity()
            .sslSocketFactory(tlsMaterial.sslSocketFactory)

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
        channelPolicy: EmergencyAuthorityTransportConfig.ChannelPolicy
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
                "Updated Android security provider is required before secure emergency authority communication.",
                e
            )
        } catch (e: GooglePlayServicesNotAvailableException) {
            throw SecurityException(
                "Android security provider could not be updated for secure emergency authority communication.",
                e
            )
        }
    }
}