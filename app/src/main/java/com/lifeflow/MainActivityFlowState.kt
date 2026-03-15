package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal const val NO_ACTION_RECORDED = "—"

private const val NO_ACTION_RECORDED_MESSAGE = "No action recorded yet."

internal data class MainActivityScreenSnapshot(
    val uiState: UiState,
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

internal fun permissionResultMessage(grantedPermissions: Set<String>): String {
    return if (grantedPermissions.isEmpty()) {
        "HC callback: no permissions granted; refresh requested"
    } else {
        "HC callback: ${grantedPermissions.size} granted; refresh requested"
    }
}

internal fun requiresVaultReset(message: String): Boolean {
    return message.contains(
        "Reset vault is required",
        ignoreCase = true
    ) || message.contains(
        "Security compromised",
        ignoreCase = true
    )
}

internal fun resolveDisplayedLastAction(
    uiAction: String,
    viewModelAction: String
): String {
    val normalizedUiAction = normalizeAction(uiAction)
    val normalizedViewModelAction = normalizeAction(viewModelAction)

    return when {
        normalizedUiAction == null && normalizedViewModelAction == null ->
            NO_ACTION_RECORDED_MESSAGE

        normalizedUiAction != null && normalizedViewModelAction == null ->
            normalizedUiAction

        normalizedUiAction == null && normalizedViewModelAction != null ->
            normalizedViewModelAction

        normalizedUiAction == normalizedViewModelAction ->
            normalizedUiAction ?: NO_ACTION_RECORDED_MESSAGE

        else ->
            "$normalizedUiAction → $normalizedViewModelAction"
    }
}

private fun normalizeAction(action: String): String? {
    return action.takeIf {
        it.isNotBlank() && it != NO_ACTION_RECORDED
    }
}