package com.lifeflow

import com.lifeflow.domain.security.TrustState

internal class MainViewModelAuthDelegate(
    private val currentSecuritySnapshot: () -> MainViewModelSecuritySnapshot,
    private val clearSession: () -> Unit,
    private val wipeUiCachesFailClosed: () -> Unit,
    private val setSessionExpiryNotified: (Boolean) -> Unit,
    private val setUiStateError: (String) -> Unit,
    private val updateLastAction: (String) -> Unit,
    private val currentUiState: () -> UiState = { UiState.Loading }
) {

    private fun applyFailClosedState(clearSession: Boolean) {
        if (clearSession) {
            clearSession()
        }

        wipeUiCachesFailClosed()
        setSessionExpiryNotified(false)
    }

    fun failClosedWithError(
        message: String,
        clearSession: Boolean = true
    ) {
        applyFailClosedState(clearSession = clearSession)
        setUiStateError(message)
        updateLastAction("Fail-closed: $message")
    }

    fun ensureProtectedEntryAllowed(): Boolean {
        val blockedMessage = currentSecuritySnapshot().protectedEntryBlockMessage()

        if (blockedMessage != null) {
            failClosedWithError(blockedMessage)
            return false
        }

        updateLastAction("Authentication verified. Bootstrapping identity...")
        return true
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
        failClosedWithError(
            message = message,
            clearSession = true
        )

    fun completeAuthenticationBootstrapError(message: String) = failClosedWithError(message)

    fun handleObservedTrustState(trustState: TrustState) =
        handleTrustUpdate(
            resolveMainViewModelTrustUpdate(
                trustState = trustState,
                uiState = currentUiState()
            )
        )

    fun handleTrustUpdate(update: MainViewModelTrustUpdate) =
        when (update) {
            is MainViewModelTrustUpdate.FailClosed -> failClosedWithError(update.message)
            is MainViewModelTrustUpdate.LastAction -> updateLastAction(update.message)
            MainViewModelTrustUpdate.NoOp -> Unit
        }

    fun handleAuthenticationError(message: String) = failClosedWithError(message)

    fun handleSessionPollTick(alreadyNotified: Boolean) {
        if (!currentSecuritySnapshot().shouldExpireSession(currentUiState())) {
            clearSessionExpiryNotification()
            return
        }

        handleSessionExpiryIfNeeded(alreadyNotified)
    }

    fun handleSessionExpiryIfNeeded(alreadyNotified: Boolean) {
        if (alreadyNotified) {
            return
        }

        setSessionExpiryNotified(true)
        failClosedWithError(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE)
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