package com.lifeflow

import com.lifeflow.domain.security.TrustState

internal class MainViewModelAuthDelegate(
    private val authRuntime: MainViewModelAuthRuntime,
    private val clearSession: () -> Unit,
    private val wipeUiCachesFailClosed: () -> Unit,
    private val setSessionExpiryNotified: (Boolean) -> Unit,
    private val setUiStateError: (String) -> Unit,
    private val updateLastAction: (String) -> Unit
) {

    private fun applyFailClosedState(clearSession: Boolean) {
        if (clearSession) {
            clearSession()
        }

        wipeUiCachesFailClosed()
        setSessionExpiryNotified(false)
    }

    private fun failClosedAuthentication(message: String, clearSession: Boolean = true) {
        failClosedWithError(
            message = message,
            clearSession = clearSession
        )
    }

    fun failClosedWithError(
        message: String,
        clearSession: Boolean = true
    ) {
        applyFailClosedState(clearSession = clearSession)
        setUiStateError(message)
        updateLastAction("Fail-closed: $message")
    }

    fun ensureRuntimeEntryAllowed(): Boolean =
        when (val decision = authRuntime.runtimeEntryDecision()) {
            is MainViewModelRuntimeEntryDecision.Allowed -> {
                updateLastAction(decision.lastActionMessage)
                true
            }

            is MainViewModelRuntimeEntryDecision.Blocked -> {
                failClosedAuthentication(decision.message)
                false
            }
        }

    fun beginAuthenticationSuccessFlow(setUiStateLoading: () -> Unit) =
        setUiStateLoading().also {
            updateLastAction("Authentication succeeded. Preparing protected dashboard...")
        }

    fun completeAuthenticationBootstrapSuccess(markAuthenticated: () -> Unit) =
        markAuthenticated().also {
            updateLastAction("Protected dashboard unlocked.")
        }

    fun completeAuthenticationBootstrapLocked(message: String) =
        failClosedAuthentication(message)

    fun completeAuthenticationBootstrapError(message: String) =
        failClosedAuthentication(message)

    fun handleObservedTrustState(trustState: TrustState) =
        handleTrustUpdate(authRuntime.resolveTrustUpdate(trustState))

    fun handleTrustUpdate(update: MainViewModelTrustUpdate) =
        when (update) {
            is MainViewModelTrustUpdate.FailClosed -> failClosedWithError(update.message)
            is MainViewModelTrustUpdate.LastAction -> updateLastAction(update.message)
            MainViewModelTrustUpdate.NoOp -> Unit
        }

    fun handleAuthenticationError(message: String) =
        failClosedAuthentication(message)

    fun handleSessionPollTick(alreadyNotified: Boolean) {
        if (authRuntime.shouldExpireSession()) {
            handleSessionExpiryIfNeeded(alreadyNotified)
            return
        }

        clearSessionExpiryNotification()
    }

    fun handleSessionExpiryIfNeeded(alreadyNotified: Boolean) {
        if (alreadyNotified) {
            return
        }

        setSessionExpiryNotified(true)
        failClosedAuthentication(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE)
    }

    fun clearSessionExpiryNotification() = setSessionExpiryNotified(false)

    fun beginVaultResetFlow(setUiStateLoading: () -> Unit) =
        setUiStateLoading().also {
            updateLastAction("Vault reset requested.")
        }

    fun completeVaultReset(
        isSuccess: Boolean,
        failureMessage: String?
    ) {
        applyFailClosedState(clearSession = true)

        if (isSuccess) {
            updateLastAction("Vault reset complete. Fresh authentication required.")
            setUiStateError("Vault reset complete. Please authenticate again.")
            return
        }

        updateLastAction("Vault reset failed.")
        setUiStateError("Vault reset failed: ${failureMessage ?: "unknown"}")
    }
}
