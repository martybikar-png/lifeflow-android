package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal class MainViewModelWellbeingState {
    val healthConnectState = mutableStateOf<HealthConnectUiState>(HealthConnectUiState.Unknown)
    val requiredHealthPermissions = mutableStateOf<Set<String>>(emptySet())
    val grantedHealthPermissions = mutableStateOf<Set<String>>(emptySet())
    val healthPermissionsInitError = mutableStateOf<String?>(null)
    val digitalTwinState = mutableStateOf<DigitalTwinState?>(null)
    val wellbeingAssessment = mutableStateOf<WellbeingAssessment?>(null)
}
