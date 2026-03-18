package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal const val NO_ACTION_RECORDED = "—"

internal data class MainActivityScreenSnapshot(
    val uiState: UiState,
    val isAuthenticating: Boolean,
    val healthState: HealthConnectUiState,
    val digitalTwinState: DigitalTwinState?,
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val stepsGranted: Boolean,
    val hrGranted: Boolean,
    val displayedLastAction: String,
    val debugLines: List<String>
)

internal fun collectMainActivityScreenSnapshot(
    viewModel: MainViewModel,
    uiLastAction: String
): MainActivityScreenSnapshot {
    val uiState = viewModel.uiState.value
    val isAuthenticating = viewModel.isSessionAuthorizedForUi()
    val healthState = viewModel.healthConnectState.value
    val digitalTwinState = viewModel.digitalTwinState.value
    val viewModelLastAction = viewModel.lastAction.value
    val requiredPermissions = viewModel.requiredHealthPermissions.value
    val grantedPermissions = viewModel.grantedHealthPermissions.value

    val stepsReadPerm = HealthPermission.getReadPermission(StepsRecord::class)
    val hrReadPerm = HealthPermission.getReadPermission(HeartRateRecord::class)

    val stepsGranted = grantedPermissions.contains(stepsReadPerm)
    val hrGranted = grantedPermissions.contains(hrReadPerm)

    return MainActivityScreenSnapshot(
        uiState = uiState,
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        digitalTwinState = digitalTwinState,
        requiredPermissions = requiredPermissions,
        grantedPermissions = grantedPermissions,
        stepsGranted = stepsGranted,
        hrGranted = hrGranted,
        displayedLastAction = resolveDisplayedLastAction(
            uiAction = uiLastAction,
            viewModelAction = viewModelLastAction
        ),
        debugLines = buildDebugLines(
            uiState = uiState,
            healthState = healthState,
            requiredCount = requiredPermissions.size,
            grantedCount = grantedPermissions.size,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted,
            digitalTwinState = digitalTwinState
        )
    )
}
