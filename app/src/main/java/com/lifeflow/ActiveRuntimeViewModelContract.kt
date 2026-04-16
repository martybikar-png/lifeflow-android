package com.lifeflow

import androidx.compose.runtime.State
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

interface ActiveRuntimeViewModelContract {
    val uiState: State<UiState>
    val lastAction: State<String>
    val healthConnectState: State<HealthConnectUiState>
    val requiredHealthPermissions: State<Set<String>>
    val grantedHealthPermissions: State<Set<String>>
    val healthPermissionsInitError: State<String?>
    val digitalTwinState: State<DigitalTwinState?>
    val wellbeingAssessment: State<WellbeingAssessment?>
    val currentTier: State<TierState>

    fun refreshMetricsAndTwinNow()
    fun onHealthPermissionsResult(granted: Set<String>)
    fun onAuthenticationSuccess()
    fun onAuthenticationError(message: String)
    fun onAppBackgrounded()
    fun onAppForegrounded()
    fun resetVault()
    fun isSessionAuthorizedForUi(): Boolean
}
