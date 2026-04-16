package com.lifeflow.security

/**
 * Internal connectivity surface for the external emergency authority transport.
 *
 * Purpose:
 * - keep gRPC connectivity state out of the rest of the app
 * - expose a stable internal snapshot for future trust / liveness policy
 */
internal enum class EmergencyAuthorityChannelState {
    UNINITIALIZED,
    UNSUPPORTED,
    IDLE,
    CONNECTING,
    READY,
    TRANSIENT_FAILURE,
    SHUTDOWN
}

internal data class EmergencyAuthorityConnectivitySnapshot(
    val controlState: EmergencyAuthorityChannelState,
    val auditState: EmergencyAuthorityChannelState
)
