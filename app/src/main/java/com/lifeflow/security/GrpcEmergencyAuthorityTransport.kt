package com.lifeflow.security

import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyAuditRecord
import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import java.util.concurrent.TimeUnit

/**
 * gRPC transport scaffold for the external emergency authority.
 *
 * Important:
 * - transport owns secure channel construction only
 * - control and audit RPC behavior live in dedicated clients
 * - secure channel bootstrap installs the updated Android security provider first
 * - endpoint and channel policy values come from EmergencyAuthorityTransportConfig
 * - mTLS material comes from EmergencyAuthorityTlsMaterialProvider
 * - transport remains fail-closed until the real RPC contract and mTLS material are wired
 * - transport exposes explicit channel lifecycle and connectivity control for future authority session management
 */
internal class GrpcEmergencyAuthorityTransport private constructor(
    private val applicationContext: Context,
    private val config: EmergencyAuthorityTransportConfig,
    private val tlsMaterialProvider: EmergencyAuthorityTlsMaterialProvider
) : EmergencyAuthorityTransport, AutoCloseable {

    companion object {
        fun create(
            applicationContext: Context
        ): GrpcEmergencyAuthorityTransport {
            return GrpcEmergencyAuthorityTransport(
                applicationContext = applicationContext.applicationContext,
                config = EmergencyAuthorityTransportConfig.fromBuildConfig(),
                tlsMaterialProvider = UnconfiguredEmergencyAuthorityTlsMaterialProvider
            )
        }
    }

    private val controlChannelDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        buildSecureChannel(config.controlEndpoint)
    }

    private val auditChannelDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        buildSecureChannel(config.auditEndpoint)
    }

    private val controlChannel: ManagedChannel by controlChannelDelegate
    private val auditChannel: ManagedChannel by auditChannelDelegate

    private val controlClient: EmergencyAuthorityControlClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GrpcEmergencyAuthorityControlClient(controlChannel)
    }

    private val auditClient: EmergencyAuthorityAuditClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GrpcEmergencyAuthorityAuditClient(auditChannel)
    }

    override fun appendAuditRecord(record: EmergencyAuditRecord): String {
        return auditClient.appendAuditRecord(record)
    }

    override fun registerIssuedArtifact(artifact: EmergencyActivationArtifact) {
        controlClient.registerIssuedArtifact(artifact)
    }

    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        return controlClient.consumeIssuedArtifact(
            artifactId = artifactId,
            consumedAtEpochMs = consumedAtEpochMs
        )
    }

    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        controlClient.markArtifactExpiredUnused(
            artifactId = artifactId,
            reason = reason,
            expiredAtEpochMs = expiredAtEpochMs
        )
    }

    fun connectivitySnapshot(
        requestConnection: Boolean = false
    ): EmergencyAuthorityConnectivitySnapshot {
        return EmergencyAuthorityConnectivitySnapshot(
            controlState = currentChannelState(
                channelDelegate = controlChannelDelegate,
                requestConnection = requestConnection
            ),
            auditState = currentChannelState(
                channelDelegate = auditChannelDelegate,
                requestConnection = requestConnection
            )
        )
    }

    fun refreshConnectionsAfterNetworkAvailable() {
        resetConnectBackoffIfInitialized(controlChannelDelegate)
        resetConnectBackoffIfInitialized(auditChannelDelegate)
    }

    fun enterIdleForNetworkChange() {
        enterIdleIfInitialized(controlChannelDelegate)
        enterIdleIfInitialized(auditChannelDelegate)
    }

    override fun close() {
        shutdownChannelIfInitialized(controlChannelDelegate)
        shutdownChannelIfInitialized(auditChannelDelegate)
    }

    fun shutdownNow() {
        shutdownNowChannelIfInitialized(controlChannelDelegate)
        shutdownNowChannelIfInitialized(auditChannelDelegate)
    }

    fun awaitTermination(
        timeoutMs: Long
    ): Boolean {
        require(timeoutMs > 0L) { "timeoutMs must be > 0." }

        val controlTerminated = awaitTerminationIfInitialized(
            channelDelegate = controlChannelDelegate,
            timeoutMs = timeoutMs
        )
        val auditTerminated = awaitTerminationIfInitialized(
            channelDelegate = auditChannelDelegate,
            timeoutMs = timeoutMs
        )

        return controlTerminated && auditTerminated
    }

    private fun buildSecureChannel(
        endpoint: EmergencyAuthorityTransportConfig.EndpointConfig
    ): ManagedChannel {
        ensureUpdatedSecurityProvider()
        validateSecureEndpoint(endpoint)

        val tlsMaterial = tlsMaterialProvider.materialFor(endpoint)
        validateTlsMaterialPolicy(endpoint, tlsMaterial)

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

    private fun validateSecureEndpoint(
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

    private fun validateTlsMaterialPolicy(
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

    private fun currentChannelState(
        channelDelegate: Lazy<ManagedChannel>,
        requestConnection: Boolean
    ): EmergencyAuthorityChannelState {
        if (!channelDelegate.isInitialized() && !requestConnection) {
            return EmergencyAuthorityChannelState.UNINITIALIZED
        }

        val channel = channelDelegate.value

        return try {
            mapConnectivityState(
                channel.getState(requestConnection)
            )
        } catch (_: UnsupportedOperationException) {
            EmergencyAuthorityChannelState.UNSUPPORTED
        }
    }

    private fun mapConnectivityState(
        state: ConnectivityState
    ): EmergencyAuthorityChannelState {
        return when (state) {
            ConnectivityState.IDLE -> EmergencyAuthorityChannelState.IDLE
            ConnectivityState.CONNECTING -> EmergencyAuthorityChannelState.CONNECTING
            ConnectivityState.READY -> EmergencyAuthorityChannelState.READY
            ConnectivityState.TRANSIENT_FAILURE -> EmergencyAuthorityChannelState.TRANSIENT_FAILURE
            ConnectivityState.SHUTDOWN -> EmergencyAuthorityChannelState.SHUTDOWN
        }
    }

    private fun shutdownChannelIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.shutdown()
    }

    private fun shutdownNowChannelIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.shutdownNow()
    }

    private fun awaitTerminationIfInitialized(
        channelDelegate: Lazy<ManagedChannel>,
        timeoutMs: Long
    ): Boolean {
        if (!channelDelegate.isInitialized()) {
            return true
        }

        return channelDelegate.value.awaitTermination(
            timeoutMs,
            TimeUnit.MILLISECONDS
        )
    }

    private fun resetConnectBackoffIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.resetConnectBackoff()
    }

    private fun enterIdleIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.enterIdle()
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
