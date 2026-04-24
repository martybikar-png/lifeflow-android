package com.lifeflow.security

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

internal data class IntegrityTrustTlsMaterial(
    val sslSocketFactory: SSLSocketFactory,
    val hostnameVerifier: HostnameVerifier? = null
)

internal interface IntegrityTrustTlsMaterialProvider {
    fun materialFor(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): IntegrityTrustTlsMaterial
}

internal object UnconfiguredIntegrityTrustTlsMaterialProvider :
    IntegrityTrustTlsMaterialProvider {

    override fun materialFor(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): IntegrityTrustTlsMaterial {
        throw SecurityException(
            "Integrity trust mTLS material is not configured for ${endpoint.host}:${endpoint.port}. " +
                "Secure verdict transport remains fail-closed."
        )
    }
}