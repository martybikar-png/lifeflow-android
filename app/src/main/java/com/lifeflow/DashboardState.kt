package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

/**
 * Dashboard state hierarchy for clear UX decisions.
 */
internal enum class DashboardState {
    LOADING,           // Initial load, no data yet
    HC_UNAVAILABLE,    // Health Connect not available
    NEEDS_PERMISSIONS, // HC available but permissions missing
    NO_DATA,          // Permissions OK but no data yet
    ATTENTION,        // Data available but needs attention
    READY             // All good
}

internal fun resolveDashboardState(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?,
    wellbeingAssessment: WellbeingAssessment?
): DashboardState {
    // Health Connect not available
    if (healthState != HealthConnectUiState.Available) {
        return DashboardState.HC_UNAVAILABLE
    }
    
    // Missing permissions
    if (requiredCount > 0 && grantedCount < requiredCount) {
        return DashboardState.NEEDS_PERMISSIONS
    }
    
    // No data loaded yet
    if (digitalTwinState == null) {
        return DashboardState.LOADING
    }
    
    // Check if we have actual data
    val hasStepsData = digitalTwinState.stepsLast24h != null
    val hasHrData = digitalTwinState.avgHeartRateLast24h != null
    
    if (!hasStepsData && !hasHrData) {
        return DashboardState.NO_DATA
    }
    
    // Check wellbeing assessment
    if (wellbeingAssessment != null) {
        when (wellbeingAssessment.overallReadiness) {
            OverallReadiness.ATTENTION_REQUIRED,
            OverallReadiness.LOW -> return DashboardState.ATTENTION
            else -> {}
        }
    }
    
    return DashboardState.READY
}

internal fun dashboardWelcomeTitle(state: DashboardState): String {
    return when (state) {
        DashboardState.LOADING -> "Loading your snapshot..."
        DashboardState.HC_UNAVAILABLE -> "Health Connect needed"
        DashboardState.NEEDS_PERMISSIONS -> "Almost there"
        DashboardState.NO_DATA -> "Waiting for data"
        DashboardState.ATTENTION -> "Check your wellbeing"
        DashboardState.READY -> "You're on track"
    }
}

internal fun dashboardWelcomeMessage(state: DashboardState): String {
    return when (state) {
        DashboardState.LOADING -> 
            "We're preparing your first wellbeing snapshot. This only takes a moment."
        DashboardState.HC_UNAVAILABLE -> 
            "To show your wellbeing data, LifeFlow needs Health Connect. Open settings to get started."
        DashboardState.NEEDS_PERMISSIONS -> 
            "Grant health access to unlock your full wellbeing picture. Your data stays on your device."
        DashboardState.NO_DATA -> 
            "Health Connect is ready, but there's no recent data yet. Check back after your device syncs."
        DashboardState.ATTENTION -> 
            "Your recent data suggests something worth reviewing. Take a moment to check the details."
        DashboardState.READY -> 
            "Your wellbeing snapshot is current. Everything looks good."
    }
}

internal fun dashboardPrimaryActionLabel(state: DashboardState): String? {
    return when (state) {
        DashboardState.HC_UNAVAILABLE -> "Open Health Connect settings"
        DashboardState.NEEDS_PERMISSIONS -> "Review health access"
        DashboardState.LOADING, DashboardState.NO_DATA -> "Refresh now"
        DashboardState.ATTENTION -> "View details"
        DashboardState.READY -> null // No urgent action needed
    }
}
