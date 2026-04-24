package com.lifeflow

import com.lifeflow.domain.security.TrustState
import com.lifeflow.security.AUTH_REQUIRED_USER_MESSAGE
import com.lifeflow.security.SECURITY_COMPROMISED_USER_MESSAGE
import com.lifeflow.security.SECURITY_DEGRADED_USER_MESSAGE
import com.lifeflow.security.SECURITY_EMERGENCY_LIMITED_USER_MESSAGE
import com.lifeflow.security.lockedReasonToUserMessage

internal const val MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE =
    "Protected refresh failed unexpectedly. Please authenticate again."
internal const val MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE =
    "Session expired. Please authenticate again."
internal const val MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE =
    "Protected refresh blocked until authentication is valid."

private fun runtimeEntryTrustStateBlockMessage(
    trustState: TrustState
): String? =
    when (trustState) {
        TrustState.COMPROMISED -> SECURITY_COMPROMISED_USER_MESSAGE
        TrustState.DEGRADED -> SECURITY_DEGRADED_USER_MESSAGE
        TrustState.EMERGENCY_LIMITED -> SECURITY_EMERGENCY_LIMITED_USER_MESSAGE
        TrustState.VERIFIED -> null
    }

private fun requiresSessionExpiryForTrustState(
    trustState: TrustState
): Boolean =
    when (trustState) {
        TrustState.COMPROMISED,
        TrustState.DEGRADED,
        TrustState.EMERGENCY_LIMITED,
        TrustState.VERIFIED -> true
    }

internal fun mainViewModelSecurityEvaluation(
    isAuthenticatedUi: Boolean,
    isAuthorized: Boolean,
    trustState: TrustState
): MainViewModelSecurityEvaluation {
    val canExposeProtectedUiData =
        isAuthenticatedUi &&
            isAuthorized &&
            trustState == TrustState.VERIFIED

    val runtimeEntryBlockMessage =
        if (!isAuthorized) {
            AUTH_REQUIRED_USER_MESSAGE
        } else {
            runtimeEntryTrustStateBlockMessage(trustState)
        }

    val shouldExpireSession =
        isAuthenticatedUi &&
            !isAuthorized &&
            requiresSessionExpiryForTrustState(trustState)

    return MainViewModelSecurityEvaluation(
        canExposeProtectedUiData = canExposeProtectedUiData,
        runtimeEntryBlockMessage = runtimeEntryBlockMessage,
        shouldExpireSession = shouldExpireSession
    )
}

internal fun mainViewModelTrustStateMessageOrNull(
    trustState: TrustState,
    isAuthenticatedUi: Boolean
): String? =
    when (trustState) {
        TrustState.COMPROMISED ->
            SECURITY_COMPROMISED_USER_MESSAGE

        TrustState.DEGRADED ->
            if (isAuthenticatedUi) {
                SECURITY_DEGRADED_USER_MESSAGE
            } else {
                null
            }

        TrustState.EMERGENCY_LIMITED ->
            if (isAuthenticatedUi) {
                SECURITY_EMERGENCY_LIMITED_USER_MESSAGE
            } else {
                null
            }

        TrustState.VERIFIED ->
            null
    }

internal fun mainViewModelLockedReasonToUserMessage(reason: String): String =
    lockedReasonToUserMessage(reason)
