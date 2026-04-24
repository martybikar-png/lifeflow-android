package com.lifeflow

import com.lifeflow.domain.security.TrustState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal fun observeMainViewModelTrustState(
    scope: CoroutineScope,
    trustStatePort: TrustStatePort,
    onTrustStateObserved: (TrustState) -> Unit
) {
    scope.launch {
        trustStatePort.observeTrustState().collect { state ->
            onTrustStateObserved(state)
        }
    }
}

internal fun observeMainViewModelSessionExpiry(
    scope: CoroutineScope,
    sessionPollMs: Long,
    isSessionExpiryNotified: () -> Boolean,
    onSessionPollTick: (Boolean) -> Unit
) {
    scope.launch {
        while (isActive) {
            delay(sessionPollMs)
            onSessionPollTick(isSessionExpiryNotified())
        }
    }
}

internal fun consumeMainViewModelPendingForegroundRefresh(
    pendingForegroundRefresh: Boolean,
    updatePendingForegroundRefresh: (Boolean) -> Unit
): Boolean {
    val shouldRefresh = pendingForegroundRefresh
    updatePendingForegroundRefresh(false)
    return shouldRefresh
}

internal fun handleMainViewModelSessionPollTick(
    securityEvaluation: MainViewModelSecurityEvaluation,
    alreadyNotified: Boolean,
    handleSessionExpiryIfNeeded: (Boolean) -> Unit,
    clearSessionExpiryNotification: () -> Unit
) {
    if (securityEvaluation.shouldExpireSession) {
        handleSessionExpiryIfNeeded(alreadyNotified)
        return
    }

    clearSessionExpiryNotification()
}
