package com.lifeflow

import com.lifeflow.domain.security.TrustState as DomainTrustState
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import com.lifeflow.security.toDomainTrustState

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

private fun isAuthenticatedUi(uiState: UiState): Boolean =
    uiState is UiState.Authenticated

private fun runtimeEntryTrustStateBlockMessage(
    trustState: DomainTrustState
): String? =
    when (trustState) {
        DomainTrustState.COMPROMISED -> SECURITY_COMPROMISED_MESSAGE
        DomainTrustState.DEGRADED -> SECURITY_DEGRADED_MESSAGE
        DomainTrustState.VERIFIED -> null
    }

private fun requiresSessionExpiryForTrustState(
    trustState: DomainTrustState
): Boolean =
    when (trustState) {
        DomainTrustState.COMPROMISED,
        DomainTrustState.DEGRADED,
        DomainTrustState.VERIFIED -> true
    }

internal fun mainViewModelLockedReasonToUserMessage(reason: String): String =
    when {
        reason.startsWith("COMPROMISED:", ignoreCase = true) ->
            SECURITY_COMPROMISED_MESSAGE

        reason.startsWith("AUTH_REQUIRED:", ignoreCase = true) ->
            "Authentication required. Please authenticate again."

        reason.isBlank() ->
            "Access locked. Please authenticate again."

        else -> reason
    }

internal fun canMainViewModelExposeProtectedUiData(uiState: UiState): Boolean =
    canMainViewModelExposeProtectedUiData(
        uiState = uiState,
        isAuthorized = SecurityAccessSession.isAuthorized(),
        trustState = SecurityRuleEngine.getTrustState().toDomainTrustState()
    )

internal fun canMainViewModelExposeProtectedUiData(
    uiState: UiState,
    isAuthorized: Boolean,
    trustState: DomainTrustState
): Boolean =
    isAuthenticatedUi(uiState) &&
        isAuthorized &&
        trustState == DomainTrustState.VERIFIED

internal fun mainViewModelRuntimeEntryBlockMessage(
    isAuthorized: Boolean,
    trustState: TrustState
): String? =
    mainViewModelRuntimeEntryBlockMessage(
        isAuthorized = isAuthorized,
        trustState = trustState.toDomainTrustState()
    )

internal fun mainViewModelRuntimeEntryBlockMessage(
    isAuthorized: Boolean,
    trustState: DomainTrustState
): String? {
    if (!isAuthorized) {
        return AUTH_REQUIRED_MESSAGE
    }

    return runtimeEntryTrustStateBlockMessage(trustState)
}

internal fun resolveMainViewModelTrustUpdate(
    trustState: TrustState,
    uiState: UiState
): MainViewModelTrustUpdate =
    resolveMainViewModelTrustUpdate(
        trustState = trustState.toDomainTrustState(),
        uiState = uiState
    )

internal fun resolveMainViewModelTrustUpdate(
    trustState: DomainTrustState,
    uiState: UiState
): MainViewModelTrustUpdate =
    when (trustState) {
        DomainTrustState.COMPROMISED ->
            MainViewModelTrustUpdate.FailClosed(SECURITY_COMPROMISED_MESSAGE)

        DomainTrustState.DEGRADED ->
            if (isAuthenticatedUi(uiState)) {
                MainViewModelTrustUpdate.FailClosed(SECURITY_DEGRADED_MESSAGE)
            } else {
                MainViewModelTrustUpdate.NoOp
            }

        DomainTrustState.VERIFIED ->
            if (isAuthenticatedUi(uiState)) {
                MainViewModelTrustUpdate.LastAction("Security trust verified.")
            } else {
                MainViewModelTrustUpdate.NoOp
            }
    }

internal fun shouldMainViewModelExpireSession(uiState: UiState): Boolean =
    shouldMainViewModelExpireSession(
        uiState = uiState,
        isAuthorized = SecurityAccessSession.isAuthorized(),
        trustState = SecurityRuleEngine.getTrustState().toDomainTrustState()
    )

internal fun shouldMainViewModelExpireSession(
    uiState: UiState,
    isAuthorized: Boolean
): Boolean =
    shouldMainViewModelExpireSession(
        uiState = uiState,
        isAuthorized = isAuthorized,
        trustState = SecurityRuleEngine.getTrustState().toDomainTrustState()
    )

internal fun shouldMainViewModelExpireSession(
    uiState: UiState,
    isAuthorized: Boolean,
    trustState: DomainTrustState
): Boolean =
    isAuthenticatedUi(uiState) &&
        !isAuthorized &&
        requiresSessionExpiryForTrustState(trustState)
