package com.lifeflow.security

import android.content.Context
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyAuditRecord
import io.grpc.ManagedChannel

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
    applicationContext: Context,
    private val config: EmergencyAuthorityTransportConfig,
    tlsMaterialProvider: EmergencyAuthorityTlsMaterialProvider
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

    private val channelFactory = GrpcEmergencyAuthoritySecureChannelFactory(
        applicationContext = applicationContext.applicationContext,
        tlsMaterialProvider = tlsMaterialProvider
    )

    private val controlChannelDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        channelFactory.buildSecureChannel(config.controlEndpoint)
    }

    private val auditChannelDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        channelFactory.buildSecureChannel(config.auditEndpoint)
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
            controlState = GrpcEmergencyAuthorityChannelLifecycle.currentChannelState(
                channelDelegate = controlChannelDelegate,
                requestConnection = requestConnection
            ),
            auditState = GrpcEmergencyAuthorityChannelLifecycle.currentChannelState(
                channelDelegate = auditChannelDelegate,
                requestConnection = requestConnection
            )
        )
    }

    fun refreshConnectionsAfterNetworkAvailable() {
        GrpcEmergencyAuthorityChannelLifecycle.resetConnectBackoffIfInitialized(
            controlChannelDelegate
        )
        GrpcEmergencyAuthorityChannelLifecycle.resetConnectBackoffIfInitialized(
            auditChannelDelegate
        )
    }

    fun enterIdleForNetworkChange() {
        GrpcEmergencyAuthorityChannelLifecycle.enterIdleIfInitialized(
            controlChannelDelegate
        )
        GrpcEmergencyAuthorityChannelLifecycle.enterIdleIfInitialized(
            auditChannelDelegate
        )
    }

    override fun close() {
        GrpcEmergencyAuthorityChannelLifecycle.shutdownChannelIfInitialized(
            controlChannelDelegate
        )
        GrpcEmergencyAuthorityChannelLifecycle.shutdownChannelIfInitialized(
            auditChannelDelegate
        )
    }

    fun shutdownNow() {
        GrpcEmergencyAuthorityChannelLifecycle.shutdownNowChannelIfInitialized(
            controlChannelDelegate
        )
        GrpcEmergencyAuthorityChannelLifecycle.shutdownNowChannelIfInitialized(
            auditChannelDelegate
        )
    }

    fun awaitTermination(
        timeoutMs: Long
    ): Boolean {
        require(timeoutMs > 0L) { "timeoutMs must be > 0." }

        val controlTerminated =
            GrpcEmergencyAuthorityChannelLifecycle.awaitTerminationIfInitialized(
                channelDelegate = controlChannelDelegate,
                timeoutMs = timeoutMs
            )
        val auditTerminated =
            GrpcEmergencyAuthorityChannelLifecycle.awaitTerminationIfInitialized(
                channelDelegate = auditChannelDelegate,
                timeoutMs = timeoutMs
            )

        return controlTerminated && auditTerminated
    }
}