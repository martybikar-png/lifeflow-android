package com.lifeflow

import com.lifeflow.domain.security.TrustState

internal sealed interface MainViewModelRuntimeEntryDecision {
    data class Allowed(val lastActionMessage: String) : MainViewModelRuntimeEntryDecision
    data class Blocked(val message: String) : MainViewModelRuntimeEntryDecision
}

internal class MainViewModelAuthRuntime(
    private val currentSecuritySnapshot: () -> MainViewModelSecuritySnapshot,
    private val currentUiState: () -> UiState
) {
    fun runtimeEntryDecision(): MainViewModelRuntimeEntryDecision {
        val blockedMessage = currentSecuritySnapshot().runtimeEntryBlockMessage()

        return if (blockedMessage == null) {
            MainViewModelRuntimeEntryDecision.Allowed(
                lastActionMessage = "Authentication verified. Bootstrapping identity..."
            )
        } else {
            MainViewModelRuntimeEntryDecision.Blocked(
                message = blockedMessage
            )
        }
    }

    fun resolveTrustUpdate(trustState: TrustState): MainViewModelTrustUpdate =
        resolveMainViewModelTrustUpdate(
            trustState = trustState,
            uiState = currentUiState()
        )

    fun shouldExpireSession(): Boolean =
        currentSecuritySnapshot().shouldExpireSession(currentUiState())
}
