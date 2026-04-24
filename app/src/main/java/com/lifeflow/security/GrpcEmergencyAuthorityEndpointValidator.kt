package com.lifeflow.security

internal class GrpcEmergencyAuthorityEndpointValidator {
    fun validateSecureEndpoint(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig
    ) {
        val host = endpoint.host.trim()
        val policy = endpoint.securityPolicy

        require("://" !in host) {
            "Emergency authority host must not include a URI scheme: $host"
        }
        require('/' !in host && '?' !in host && '#' !in host) {
            "Emergency authority host must not include URI path/query/fragment: $host"
        }

        if (!policy.allowPlaceholderHost && host.endsWith(".invalid", ignoreCase = true)) {
            throw SecurityException(
                "Emergency authority host remains placeholder/unconfigured: $host"
            )
        }

        if (!policy.allowLoopbackHost && isLoopbackHost(host)) {
            throw SecurityException(
                "Emergency authority loopback host is not allowed for secure transport: $host"
            )
        }

        if (!policy.allowIpLiteralHost && isIpLiteralHost(host)) {
            throw SecurityException(
                "Emergency authority IP-literal host is not allowed for secure transport: $host"
            )
        }
    }

    fun validateTlsMaterialPolicy(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig,
        tlsMaterial: EmergencyAuthorityTlsMaterial
    ) {
        if (tlsMaterial.hostnameVerifier != null &&
            !endpoint.securityPolicy.allowCustomHostnameVerifier
        ) {
            throw SecurityException(
                "Custom hostname verifier is not allowed for emergency authority endpoint ${endpoint.host}."
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