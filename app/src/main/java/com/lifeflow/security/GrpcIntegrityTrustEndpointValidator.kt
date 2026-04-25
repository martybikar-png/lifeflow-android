package com.lifeflow.security

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

internal class GrpcIntegrityTrustEndpointValidator {
    fun exactHostnameVerifier(
        expectedHost: String
    ): HostnameVerifier {
        val defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier()

        return HostnameVerifier { requestedHost, session ->
            requestedHost.equals(expectedHost, ignoreCase = true) &&
                defaultVerifier.verify(expectedHost, session)
        }
    }

    fun validateSecureEndpoint(
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

    fun validateTlsMaterialPolicy(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig,
        tlsMaterial: IntegrityTrustTlsMaterial
    ) {
        if (tlsMaterial.hostnameVerifier != null &&
            !endpoint.securityPolicy.allowCustomHostnameVerifier
        ) {
            throw SecurityException(
                "Custom hostname verifier is not allowed for integrity trust endpoint ${endpoint.host}."
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
}