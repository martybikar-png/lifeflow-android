package com.lifeflow

import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState

internal const val MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE =
    "Protected refresh failed unexpectedly. Please authenticate again."
internal const val MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE =
    "Session expired. Please authenticate again."
internal const val MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE =
    "Protected refresh blocked until authentication is valid."

private const val SECURITY_COMPROMISED_MESSAGE =
    "Security compromised. Reset vault is required before continuing."
private const val SECURITY_DEGRADED_MESSAGE =
    "Security degraded. Please authenticate again."
private const val AUTH_REQUIRED_MESSAGE =
    "Active auth session missing. Please authenticate again."

internal sealed interface MainViewModelTrustUpdate {
    data object NoOp : MainViewModelTrustUpdate
    data class FailClosed(val message: String) : MainViewModelTrustUpdate
    data class LastAction(val message: String) : MainViewModelTrustUpdate
}

internal fun mainViewModelLockedReasonToUserMessage(reason: String): String {
    return when {
        reason.startsWith("COMPROMISED:", ignoreCase = true) ->
            SECURITY_COMPROMISED_MESSAGE

        reason.startsWith("AUTH_REQUIRED:", ignoreCase = true) ->
            "Authentication required. Please authenticate again."

        reason.isBlank() ->
            "Access locked. Please authenticate again."

        else -> reason
    }
}

internal fun canMainViewModelExposeProtectedUiData(uiState: UiState): Boolean {
    return uiState is UiState.Authenticated &&
            SecurityAccessSession.isAuthorized() &&
            SecurityRuleEngine.getTrustState() == TrustState.VERIFIED
}

internal fun mainViewModelProtectedEntryBlockMessage(
    isAuthorized: Boolean,
    trustState: TrustState
): String? {
    if (!isAuthorized) {
        return AUTH_REQUIRED_MESSAGE
    }

    return when (trustState) {
        TrustState.COMPROMISED -> SECURITY_COMPROMISED_MESSAGE
        TrustState.DEGRADED -> SECURITY_DEGRADED_MESSAGE
        TrustState.VERIFIED -> null
    }
}

internal fun resolveMainViewModelTrustUpdate(
    trustState: TrustState,
    uiState: UiState
): MainViewModelTrustUpdate {
    return when (trustState) {
        TrustState.COMPROMISED -> {
            MainViewModelTrustUpdate.FailClosed(SECURITY_COMPROMISED_MESSAGE)
        }

        TrustState.DEGRADED -> {
            if (uiState is UiState.Authenticated) {
                MainViewModelTrustUpdate.FailClosed(SECURITY_DEGRADED_MESSAGE)
            } else {
                MainViewModelTrustUpdate.NoOp
            }
        }

        TrustState.VERIFIED -> {
            if (uiState is UiState.Authenticated) {
                MainViewModelTrustUpdate.LastAction("Security trust verified.")
            } else {
                MainViewModelTrustUpdate.NoOp
            }
        }
    }
}

internal fun shouldMainViewModelExpireSession(uiState: UiState): Boolean {
    return uiState is UiState.Authenticated && !SecurityAccessSession.isAuthorized()
}