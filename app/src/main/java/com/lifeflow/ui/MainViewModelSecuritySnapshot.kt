package com.lifeflow

import com.lifeflow.domain.security.TrustState

internal data class MainViewModelSecuritySnapshot(
    val isAuthorized: Boolean,
    val trustState: TrustState
)

internal fun MainViewModelSecuritySnapshot.canExposeProtectedUiData(
    uiState: UiState
): Boolean {
    return canMainViewModelExposeProtectedUiData(
        uiState = uiState,
        isAuthorized = isAuthorized,
        trustState = trustState
    )
}

internal fun MainViewModelSecuritySnapshot.protectedEntryBlockMessage(): String? {
    return mainViewModelProtectedEntryBlockMessage(
        isAuthorized = isAuthorized,
        trustState = trustState
    )
}

internal fun MainViewModelSecuritySnapshot.shouldExpireSession(
    uiState: UiState
): Boolean {
    return shouldMainViewModelExpireSession(
        uiState = uiState,
        isAuthorized = isAuthorized
    )
}