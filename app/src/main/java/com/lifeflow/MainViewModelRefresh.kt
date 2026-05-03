package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.WellbeingRefreshSnapshot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal fun applyMainViewModelWellbeingSnapshot(
    snapshot: WellbeingRefreshSnapshot,
    wellbeingState: MainViewModelWellbeingState,
    canExposeProtectedUiDataNow: Boolean,
    updateLastAction: (String) -> Unit
) {
    val update = if (canExposeProtectedUiDataNow) {
        mainViewModelProtectedSnapshotUiUpdate(snapshot)
    } else {
        mainViewModelProtectedSnapshotFailClosedUiUpdate(snapshot)
    }

    applyMainViewModelWellbeingUiUpdate(
        update = update,
        healthConnectStateState = wellbeingState.healthConnectState,
        requiredHealthPermissionsState = wellbeingState.requiredHealthPermissions,
        grantedHealthPermissionsState = wellbeingState.grantedHealthPermissions,
        healthPermissionsInitErrorState = wellbeingState.healthPermissionsInitError,
        digitalTwinStateState = wellbeingState.digitalTwinState,
        wellbeingAssessmentState = wellbeingState.wellbeingAssessment,
        updateLastAction = updateLastAction
    )
}

internal fun handleMainViewModelUnexpectedProtectedRefreshFailure(
    wasAuthenticated: Boolean,
    refreshPublicHealthStateOnly: () -> Unit,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    refreshPublicHealthStateOnly()

    if (wasAuthenticated) {
        failClosedWithError(MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE, true)
    } else {
        updateLastAction("Protected refresh failed unexpectedly.")
    }
}

internal suspend fun refreshMainViewModelWellbeingSnapshotSafe(
    identityInitialized: Boolean,
    refreshMutex: Mutex,
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    canExposeProtectedUiDataNow: () -> Boolean,
    updateLastAction: (String) -> Unit,
    refreshPublicHealthStateWithMessage: suspend (String) -> Unit,
    failClosedWithError: (String, Boolean) -> Unit
) {
    refreshMutex.withLock {
        if (!canExposeProtectedUiDataNow()) {
            refreshPublicHealthStateWithMessage(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE)
            return@withLock
        }

        updateLastAction("Refreshing protected wellbeing snapshot...")

        when (val result = wellbeingRuntime.refreshWellbeingSnapshot(identityInitialized)) {
            is ActionResult.Success -> {
                applyMainViewModelWellbeingSnapshot(
                    snapshot = result.value,
                    wellbeingState = wellbeingState,
                    canExposeProtectedUiDataNow = canExposeProtectedUiDataNow(),
                    updateLastAction = updateLastAction
                )
            }

            is ActionResult.Error -> {
                refreshPublicHealthStateWithMessage(
                    "Protected refresh failed. Public state kept available."
                )
            }

            is ActionResult.Locked -> {
                failClosedWithError(
                    mainViewModelLockedReasonToUserMessage(result.reason),
                    true
                )
            }
        }
    }
}

internal suspend fun refreshMainViewModelProtectedSnapshot(
    identityInitialized: Boolean,
    refreshWellbeingSnapshotSafe: suspend (Boolean) -> Unit,
    handleUnexpectedProtectedRefreshFailure: () -> Unit
) {
    runCatching {
        refreshWellbeingSnapshotSafe(identityInitialized)
    }.onFailure {
        handleUnexpectedProtectedRefreshFailure()
    }
}

internal suspend fun triggerMainViewModelRuntimeRefresh(
    lastActionMessage: String,
    updateLastAction: (String) -> Unit,
    refreshTierAndBoundaryState: () -> Unit,
    isFreeTier: () -> Boolean,
    refreshPublicHealthStateOnly: suspend () -> Unit,
    refreshProtectedSnapshot: suspend (Boolean) -> Unit,
    isAuthenticatedUiNow: () -> Boolean
) {
    updateLastAction(lastActionMessage)
    refreshTierAndBoundaryState()

    if (isFreeTier()) {
        refreshPublicHealthStateOnly()
        return
    }

    refreshProtectedSnapshot(isAuthenticatedUiNow())
}
