package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal fun applyMainViewModelWellbeingUiUpdate(
    update: MainViewModelWellbeingUiUpdate,
    healthConnectStateState: MutableState<HealthConnectUiState>,
    requiredHealthPermissionsState: MutableState<Set<String>>,
    grantedHealthPermissionsState: MutableState<Set<String>>,
    healthPermissionsInitErrorState: MutableState<String?>,
    digitalTwinStateState: MutableState<DigitalTwinState?>,
    wellbeingAssessmentState: MutableState<WellbeingAssessment?>,
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
    if (update.updateWellbeingAssessment) {
        wellbeingAssessmentState.value = update.wellbeingAssessment
    }
    updateLastAction(update.lastAction)
}
