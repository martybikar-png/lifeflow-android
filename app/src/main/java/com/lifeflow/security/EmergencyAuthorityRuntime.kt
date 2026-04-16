package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import com.lifeflow.domain.security.EmergencyAuditSinkPort

/**
 * Central runtime holder for the emergency authority boundary.
 *
 * Purpose:
 * - keep application wiring out of transport details
 * - hold one shared production transport instance
 * - keep lifecycle and future connectivity hooks in one place
 */
internal class EmergencyAuthorityRuntime internal constructor(
    val emergencyAuditSink: EmergencyAuditSinkPort,
    val emergencyArtifactRegistry: EmergencyArtifactRegistryPort,
    private val transport: GrpcEmergencyAuthorityTransport?
) : EmergencyAuthorityBoundaryHandle {

    fun connectivitySnapshot(
        requestConnection: Boolean = false
    ): EmergencyAuthorityConnectivitySnapshot? {
        return transport?.connectivitySnapshot(requestConnection)
    }

    fun refreshConnectionsAfterNetworkAvailable() {
        transport?.refreshConnectionsAfterNetworkAvailable()
    }

    fun enterIdleForNetworkChange() {
        transport?.enterIdleForNetworkChange()
    }

    override fun close() {
        transport?.close()
    }

    fun shutdownNow() {
        transport?.shutdownNow()
    }

    fun awaitTermination(
        timeoutMs: Long
    ): Boolean {
        require(timeoutMs > 0L) { "timeoutMs must be > 0." }
        return transport?.awaitTermination(timeoutMs) ?: true
    }
}
