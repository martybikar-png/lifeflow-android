package com.lifeflow.security

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import java.util.concurrent.TimeUnit

internal object GrpcEmergencyAuthorityChannelLifecycle {
    fun currentChannelState(
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

    fun shutdownChannelIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.shutdown()
    }

    fun shutdownNowChannelIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.shutdownNow()
    }

    fun awaitTerminationIfInitialized(
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

    fun resetConnectBackoffIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.resetConnectBackoff()
    }

    fun enterIdleIfInitialized(
        channelDelegate: Lazy<ManagedChannel>
    ) {
        if (!channelDelegate.isInitialized()) {
            return
        }

        channelDelegate.value.enterIdle()
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
}