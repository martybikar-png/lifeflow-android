package com.lifeflow

import androidx.compose.runtime.Composable
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

@Composable
internal fun AuthenticatedDashboardScreen(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    digitalTwinState: DigitalTwinState?,
    wellbeingAssessment: WellbeingAssessment?,
    lastAction: String,
    onRefreshNow: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onReAuthenticate: () -> Unit,
    debugLines: List<String>
) {
    val dashboardState = resolveDashboardState(
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount,
        digitalTwinState = digitalTwinState,
        wellbeingAssessment = wellbeingAssessment
    )

    ScreenContainer(title = "LifeFlow Dashboard") {
        // Welcome card with state-aware messaging
        DashboardWelcomeCard(
            state = dashboardState,
            onPrimaryAction = {
                when (dashboardState) {
                    DashboardState.HC_UNAVAILABLE -> onOpenHealthConnectSettings()
                    DashboardState.NEEDS_PERMISSIONS -> onGrantHealthPermissions()
                    DashboardState.LOADING, DashboardState.NO_DATA -> onRefreshNow()
                    DashboardState.ATTENTION, DashboardState.READY -> onRefreshNow()
                }
            }
        )

        ScreenSectionSpacer()

        // Priority: Show actions first if user needs to do something
        if (dashboardState in listOf(
                DashboardState.HC_UNAVAILABLE,
                DashboardState.NEEDS_PERMISSIONS
            )
        ) {
            DashboardActionsCard(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onReAuthenticate
            )
            ScreenSectionSpacer()
        }

        // Wellbeing first when we have data
        if (wellbeingAssessment != null) {
            WellbeingAssessmentCard(
                wellbeingAssessment = wellbeingAssessment
            )
            ScreenSectionSpacer()
        }

        // Digital Twin
        DigitalTwinCard(
            digitalTwinState = digitalTwinState
        )

        ScreenSectionSpacer()

        // Health summary
        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        ScreenSectionSpacer()

        // Dashboard status (detailed)
        DashboardStatusCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            digitalTwinState = digitalTwinState
        )

        // Actions at bottom if not shown at top
        if (dashboardState !in listOf(
                DashboardState.HC_UNAVAILABLE,
                DashboardState.NEEDS_PERMISSIONS
            )
        ) {
            ScreenSectionSpacer()
            DashboardActionsCard(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onReAuthenticate
            )
        }

        ScreenFooter(
            lastAction = lastAction,
            debugLines = debugLines
        )
    }
}
