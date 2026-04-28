package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal data class ActiveRuntimeScreenSnapshot(
    val uiState: UiState,
    val isAuthenticating: Boolean,
    val healthState: HealthConnectUiState,
    val digitalTwinState: DigitalTwinState?,
    val wellbeingAssessment: WellbeingAssessment?,
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val stepsGranted: Boolean,
    val hrGranted: Boolean,
    val boundarySnapshot: MainBoundarySnapshot,
    val freeTierMessage: String,
    val lastAction: String
)

internal fun collectActiveRuntimeScreenSnapshot(
    viewModel: ActiveRuntimeViewModelContract
): ActiveRuntimeScreenSnapshot {
    val uiState = viewModel.uiState.value
    val isAuthenticating = viewModel.isSessionAuthorizedForUi()
    val healthState = viewModel.healthConnectState.value
    val digitalTwinState = viewModel.digitalTwinState.value
    val wellbeingAssessment = viewModel.wellbeingAssessment.value
    val boundarySnapshot = viewModel.boundarySnapshot.value
    val freeTierMessage = viewModel.freeTierMessage.value
    val lastAction = viewModel.lastAction.value
    val requiredPermissions = viewModel.requiredHealthPermissions.value
    val grantedPermissions = viewModel.grantedHealthPermissions.value
    val stepsReadPerm = HealthPermission.getReadPermission(StepsRecord::class)
    val hrReadPerm = HealthPermission.getReadPermission(HeartRateRecord::class)
    val stepsGranted = grantedPermissions.contains(stepsReadPerm)
    val hrGranted = grantedPermissions.contains(hrReadPerm)

    return ActiveRuntimeScreenSnapshot(
        uiState = uiState,
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        digitalTwinState = digitalTwinState,
        wellbeingAssessment = wellbeingAssessment,
        requiredPermissions = requiredPermissions,
        grantedPermissions = grantedPermissions,
        stepsGranted = stepsGranted,
        hrGranted = hrGranted,
        boundarySnapshot = boundarySnapshot,
        freeTierMessage = freeTierMessage,
        lastAction = lastAction
    )
}
