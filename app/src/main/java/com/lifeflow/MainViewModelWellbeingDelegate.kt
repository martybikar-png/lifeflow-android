package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.WellbeingRefreshSnapshot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class MainViewModelWellbeingDelegate(
    private val wellbeingRuntime: MainViewModelWellbeingRuntime,
    private val wellbeingState: MainViewModelWellbeingState,
    private val refreshMutex: Mutex,
    private val currentUiState: () -> UiState,
    private val currentSecuritySnapshot: () -> MainViewModelSecuritySnapshot,
    private val failClosedWithError: (String, Boolean) -> Unit,
    private val updateLastAction: (String) -> Unit
) {

    private fun canExposeProtectedUiData(): Boolean {
        return currentSecuritySnapshot().canExposeProtectedUiData(currentUiState())
    }

    private fun refreshPublicHealthStateWithMessage(message: String) {
        refreshPublicHealthStateOnly()
        updateLastAction(message)
    }

    fun refreshRequiredPermissionsDefinition() {
        val refresh = wellbeingRuntime.refreshRequiredPermissionsDefinition()

        wellbeingState.requiredHealthPermissions.value = refresh.requiredPermissions
        wellbeingState.healthPermissionsInitError.value = refresh.initError
        updateLastAction(refresh.lastActionMessage)
    }

    fun refreshHealthConnectStatusSafe() {
        val refresh = wellbeingRuntime.refreshHealthConnectStatusSafe()

        wellbeingState.healthConnectState.value = refresh.healthConnectState
        updateLastAction(refresh.lastActionMessage)
    }

    fun refreshPublicHealthStateOnly() {
        refreshHealthConnectStatusSafe()
        refreshRequiredPermissionsDefinition()

        val update = mainViewModelPublicHealthStateOnlyUiUpdate(
            healthConnectState = wellbeingState.healthConnectState.value,
            requiredHealthPermissions = wellbeingState.requiredHealthPermissions.value,
            healthPermissionsInitError = wellbeingState.healthPermissionsInitError.value
        )

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

    fun handleUnexpectedProtectedRefreshFailure() {
        val wasAuthenticated = currentUiState() is UiState.Authenticated
        refreshPublicHealthStateOnly()

        if (wasAuthenticated) {
            failClosedWithError(MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE, true)
        } else {
            updateLastAction("Protected refresh failed unexpectedly.")
        }
    }

    fun applyWellbeingSnapshot(snapshot: WellbeingRefreshSnapshot) {
        val update = if (canExposeProtectedUiData()) {
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

    suspend fun refreshWellbeingSnapshotSafe(identityInitialized: Boolean) {
        refreshMutex.withLock {
            if (!canExposeProtectedUiData()) {
                refreshPublicHealthStateWithMessage(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE)
                return@withLock
            }

            updateLastAction("Refreshing protected wellbeing snapshot...")

            when (val result = wellbeingRuntime.refreshWellbeingSnapshot(identityInitialized)) {
                is ActionResult.Success -> applyWellbeingSnapshot(result.value)

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
}
