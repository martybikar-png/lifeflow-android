package com.lifeflow

import kotlinx.coroutines.CoroutineScope

internal class MainViewModelActiveRuntimeGateway(
    private val scope: CoroutineScope,
    private val beginAuthenticationSuccessFlow: (() -> Unit) -> Unit,
    private val runAuthenticationBootstrap: suspend () -> Unit,
    private val failClosedAuthentication: (String, Boolean) -> Unit,
    private val isFreeTier: () -> Boolean,
    private val isAuthenticatedUiNow: () -> Boolean,
    private val pendingForegroundRefresh: () -> Boolean,
    private val updatePendingForegroundRefresh: (Boolean) -> Unit,
    private val currentSecurityEvaluation: () -> MainViewModelSecurityEvaluation,
    private val isSessionExpiryNotified: () -> Boolean,
    private val setSessionExpiryNotified: (Boolean) -> Unit,
    private val canExposeProtectedUiDataNow: () -> Boolean,
    private val launchRuntimeRefresh: (String) -> Unit,
    private val runVaultReset: suspend () -> Unit,
    private val updateGrantedHealthPermissions: (Set<String>) -> Unit
) {
    fun onHealthPermissionsResult(granted: Set<String>) =
        handleMainViewModelHealthPermissionsResult(
            granted = granted,
            updateGrantedHealthPermissions = updateGrantedHealthPermissions,
            launchRuntimeRefresh = launchRuntimeRefresh
        )

    fun onAuthenticationSuccess() =
        launchMainViewModelAuthenticationSuccess(
            scope = scope,
            beginAuthenticationSuccessFlow = beginAuthenticationSuccessFlow,
            runAuthenticationBootstrap = runAuthenticationBootstrap
        )

    fun onAuthenticationError(message: String) =
        handleMainViewModelAuthenticationError(
            message = message,
            failClosedAuthentication = failClosedAuthentication
        )

    fun onAppBackgrounded() =
        handleMainViewModelAppBackgrounded(
            isFreeTier = isFreeTier,
            isAuthenticatedUiNow = isAuthenticatedUiNow,
            updatePendingForegroundRefresh = updatePendingForegroundRefresh
        )

    fun onAppForegrounded() =
        handleMainViewModelAppForegrounded(
            pendingForegroundRefresh = pendingForegroundRefresh(),
            updatePendingForegroundRefresh = updatePendingForegroundRefresh,
            currentSecurityEvaluation = currentSecurityEvaluation,
            isSessionExpiryNotified = isSessionExpiryNotified,
            setSessionExpiryNotified = setSessionExpiryNotified,
            failClosedAuthentication = failClosedAuthentication,
            canExposeProtectedUiDataNow = canExposeProtectedUiDataNow,
            launchRuntimeRefresh = launchRuntimeRefresh
        )

    fun resetVault() =
        launchMainViewModelVaultReset(
            scope = scope,
            beginAuthenticationSuccessFlow = beginAuthenticationSuccessFlow,
            runVaultReset = runVaultReset
        )
}
