package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.core.WellbeingRefreshSnapshot

internal data class MainViewModelRequiredPermissionsRefresh(
    val requiredPermissions: Set<String>,
    val initError: String?,
    val lastActionMessage: String
)

internal data class MainViewModelHealthConnectStatusRefresh(
    val healthConnectState: HealthConnectUiState,
    val lastActionMessage: String
)

internal class MainViewModelWellbeingRuntime(
    private val orchestrator: LifeFlowOrchestrator
) {
    fun refreshRequiredPermissionsDefinition(): MainViewModelRequiredPermissionsRefresh {
        return when (val result = orchestrator.requiredHealthPermissionsSafe()) {
            is ActionResult.Success -> MainViewModelRequiredPermissionsRefresh(
                requiredPermissions = result.value,
                initError = null,
                lastActionMessage = "Health permission contract loaded (${result.value.size} required)."
            )

            is ActionResult.Error -> MainViewModelRequiredPermissionsRefresh(
                requiredPermissions = emptySet(),
                initError = result.message,
                lastActionMessage = "Health permission contract failed to load."
            )

            is ActionResult.Locked -> MainViewModelRequiredPermissionsRefresh(
                requiredPermissions = emptySet(),
                initError = result.reason,
                lastActionMessage = "Health permission contract locked."
            )
        }
    }

    fun refreshHealthConnectStatusSafe(): MainViewModelHealthConnectStatusRefresh {
        val state = runCatching {
            orchestrator.healthConnectUiState()
        }.getOrElse {
            HealthConnectUiState.Unknown
        }

        return MainViewModelHealthConnectStatusRefresh(
            healthConnectState = state,
            lastActionMessage = "Health Connect state checked: $state."
        )
    }

    suspend fun refreshWellbeingSnapshot(
        identityInitialized: Boolean
    ): ActionResult<WellbeingRefreshSnapshot> {
        return orchestrator.refreshWellbeingSnapshot(identityInitialized)
    }
}
