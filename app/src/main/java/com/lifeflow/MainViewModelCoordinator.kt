package com.lifeflow

import com.lifeflow.domain.security.TrustState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.CoroutineScope

internal fun initializeMainViewModelCoordinator(
    scope: CoroutineScope,
    trustStatePort: TrustStatePort,
    sessionPollMs: Long,
    isSessionExpiryNotified: () -> Boolean,
    currentSecurityEvaluation: () -> MainViewModelSecurityEvaluation,
    onTrustStateObserved: (TrustState) -> Unit,
    setSessionExpiryNotified: (Boolean) -> Unit,
    failClosedAuthentication: (String, Boolean) -> Unit
) {
    observeMainViewModelTrustState(
        scope = scope,
        trustStatePort = trustStatePort,
        onTrustStateObserved = onTrustStateObserved
    )

    observeMainViewModelSessionExpiry(
        scope = scope,
        sessionPollMs = sessionPollMs,
        isSessionExpiryNotified = isSessionExpiryNotified,
        onSessionPollTick = { alreadyNotified ->
            handleMainViewModelSessionPollTick(
                securityEvaluation = currentSecurityEvaluation(),
                alreadyNotified = alreadyNotified,
                handleSessionExpiryIfNeeded = { notified ->
                    handleMainViewModelSessionExpiryIfNeeded(
                        alreadyNotified = notified,
                        setSessionExpiryNotified = setSessionExpiryNotified,
                        failClosedAuthentication = failClosedAuthentication
                    )
                },
                clearSessionExpiryNotification = {
                    setSessionExpiryNotified(false)
                }
            )
        }
    )
}
