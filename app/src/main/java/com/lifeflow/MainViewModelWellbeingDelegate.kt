package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.ActionResult
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.core.WellbeingRefreshSnapshot
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class MainViewModelWellbeingDelegate(
    private val orchestrator: LifeFlowOrchestrator,
    private val healthConnectState: MutableState<HealthConnectUiState>,
    private val requiredHealthPermissions: MutableState<Set<String>>,
    private val grantedHealthPermissions: MutableState<Set<String>>,
    private val healthPermissionsInitError: MutableState<String?>,
    private val digitalTwinState: MutableState<DigitalTwinState?>,
    private val wellbeingAssessment: MutableState<WellbeingAssessment?>,
    private val refreshMutex: Mutex,
    private val currentUiState: () -> UiState,
    private val currentSecuritySnapshot: () -> MainViewModelSecuritySnapshot,
    private val failClosedWithError: (String, Boolean) -> Unit,
    private val updateLastAction: (String) -> Unit
) {

    fun refreshRequiredPermissionsDefinition() {
        val (requiredPermissionsValue, initErrorValue, lastActionMessage) =
            when (val res = orchestrator.requiredHealthPermissionsSafe()) {
                is ActionResult.Success -> Triple(
                    res.value, null,
                    "Health permission contract loaded (${res.value.size} required)."
                )
                is ActionResult.Error -> Triple(
                    emptySet(), res.message,
                    "Health permission contract failed to load."
                )
                is ActionResult.Locked -> Triple(
                    emptySet(), res.reason,
                    "Health permission contract locked."
                )
            }
        requiredHealthPermissions.value = requiredPermissionsValue
        healthPermissionsInitError.value = initErrorValue
        updateLastAction(lastActionMessage)
    }

    fun refreshHealthConnectStatusSafe() {
        healthConnectState.value = runCatching {
            orchestrator.healthConnectUiState()
        }.getOrElse { HealthConnectUiState.Unknown }
        updateLastAction("Health Connect state checked: ${healthConnectState.value}.")
    }

    fun refreshPublicHealthStateOnly() {
        refreshHealthConnectStatusSafe()
        refreshRequiredPermissionsDefinition()
        val update = mainViewModelPublicHealthStateOnlyUiUpdate(
            healthConnectState = healthConnectState.value,
            requiredHealthPermissions = requiredHealthPermissions.value,
            healthPermissionsInitError = healthPermissionsInitError.value
        )
        applyMainViewModelWellbeingUiUpdate(
            update = update,
            healthConnectStateState = healthConnectState,
            requiredHealthPermissionsState = requiredHealthPermissions,
            grantedHealthPermissionsState = grantedHealthPermissions,
            healthPermissionsInitErrorState = healthPermissionsInitError,
            digitalTwinStateState = digitalTwinState,
            wellbeingAssessmentState = wellbeingAssessment,
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
        val update = if (currentSecuritySnapshot().canExposeProtectedUiData(currentUiState())) {
            mainViewModelProtectedSnapshotUiUpdate(snapshot)
        } else {
            mainViewModelProtectedSnapshotFailClosedUiUpdate(snapshot)
        }
        applyMainViewModelWellbeingUiUpdate(
            update = update,
            healthConnectStateState = healthConnectState,
            requiredHealthPermissionsState = requiredHealthPermissions,
            grantedHealthPermissionsState = grantedHealthPermissions,
            healthPermissionsInitErrorState = healthPermissionsInitError,
            digitalTwinStateState = digitalTwinState,
            wellbeingAssessmentState = wellbeingAssessment,
            updateLastAction = updateLastAction
        )
    }

    suspend fun refreshWellbeingSnapshotSafe(identityInitialized: Boolean) {
        refreshMutex.withLock {
            val canExposeProtectedUiData =
                currentSecuritySnapshot().canExposeProtectedUiData(currentUiState())
            if (!canExposeProtectedUiData) {
                refreshPublicHealthStateOnly()
                updateLastAction(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE)
                return@withLock
            }
            updateLastAction("Refreshing protected wellbeing snapshot...")
            when (val res = orchestrator.refreshWellbeingSnapshot(identityInitialized)) {
                is ActionResult.Success -> applyWellbeingSnapshot(res.value)
                is ActionResult.Error -> {
                    refreshPublicHealthStateOnly()
                    updateLastAction("Protected refresh failed. Public state kept available.")
                }
                is ActionResult.Locked -> {
                    failClosedWithError(
                        mainViewModelLockedReasonToUserMessage(res.reason), true
                    )
                }
            }
        }
    }
}
