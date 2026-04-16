package com.lifeflow.security

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

/**
 * mTLS material boundary for the external emergency authority.
 *
 * Important:
 * - transport must not invent or downgrade TLS material
 * - production must fail closed until real authority-issued material is wired
 * - custom hostname verifier is optional and should remain absent unless strictly required
 */
internal data class EmergencyAuthorityTlsMaterial(
    val sslSocketFactory: SSLSocketFactory,
    val hostnameVerifier: HostnameVerifier? = null
)

internal interface EmergencyAuthorityTlsMaterialProvider {
    fun materialFor(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig
    ): EmergencyAuthorityTlsMaterial
}

internal object UnconfiguredEmergencyAuthorityTlsMaterialProvider :
    EmergencyAuthorityTlsMaterialProvider {

    override fun materialFor(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig
    ): EmergencyAuthorityTlsMaterial {
        throw SecurityException(
            "Emergency authority mTLS material is not configured for ${endpoint.host}:${endpoint.port}. " +
                "Secure authority transport remains fail-closed."
        )
    }
}
