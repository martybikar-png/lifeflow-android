package com.lifeflow

import androidx.lifecycle.viewModelScope
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class MainViewModelLifecycleRuntime(
    private val trustStatePort: TrustStatePort,
    private val shouldQueueForegroundRefresh: () -> Boolean
) {
    private var sessionExpiryNotified = false
    private var pendingForegroundRefresh = false
    private val sessionPollMs = 1000L

    fun setSessionExpiryNotified(value: Boolean) {
        sessionExpiryNotified = value
    }

    fun observeTrustState(
        scope: CoroutineScope,
        authDelegate: MainViewModelAuthDelegate
    ) {
        scope.launch {
            trustStatePort.observeTrustState().collect { state ->
                authDelegate.handleObservedTrustState(state)
            }
        }
    }

    fun observeSessionExpiry(
        scope: CoroutineScope,
        authDelegate: MainViewModelAuthDelegate
    ) {
        scope.launch {
            while (isActive) {
                delay(sessionPollMs)
                authDelegate.handleSessionPollTick(sessionExpiryNotified)
            }
        }
    }

    fun onAppBackgrounded() {
        pendingForegroundRefresh = shouldQueueForegroundRefresh()
    }

    fun consumePendingForegroundRefresh(): Boolean {
        val shouldRefresh = pendingForegroundRefresh
        pendingForegroundRefresh = false
        return shouldRefresh
    }

    fun handleSessionPollTick(authDelegate: MainViewModelAuthDelegate) {
        authDelegate.handleSessionPollTick(sessionExpiryNotified)
    }
}
