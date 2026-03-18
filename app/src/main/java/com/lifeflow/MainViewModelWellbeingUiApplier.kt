package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

internal fun applyMainViewModelWellbeingUiUpdate(
    update: MainViewModelWellbeingUiUpdate,
    healthConnectStateState: MutableState<HealthConnectUiState>,
    requiredHealthPermissionsState: MutableState<Set<String>>,
    grantedHealthPermissionsState: MutableState<Set<String>>,
    healthPermissionsInitErrorState: MutableState<String?>,
    digitalTwinStateState: MutableState<DigitalTwinState?>,
    updateLastAction: (String) -> Unit
) {
    update.healthConnectState?.let { healthConnectStateState.value = it }
    update.requiredHealthPermissions?.let { requiredHealthPermissionsState.value = it }
    update.grantedHealthPermissions?.let { grantedHealthPermissionsState.value = it }

    if (update.updateHealthPermissionsInitError) {
        healthPermissionsInitErrorState.value = update.healthPermissionsInitError
    }

    if (update.updateDigitalTwinState) {
        digitalTwinStateState.value = update.digitalTwinState
    }

    updateLastAction(update.lastAction)
}