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
    ScreenContainer(title = "LifeFlow Dashboard") {
        GuidanceCard(
            title = "Protected dashboard",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This dashboard reflects the current protected wellbeing state, Digital Twin readiness, and next safe actions without redefining trust truth or bypassing orchestration."
        )

        ScreenSectionSpacer()

        DashboardStatusCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            digitalTwinState = digitalTwinState
        )

        ScreenSectionSpacer()

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        ScreenSectionSpacer()

        DigitalTwinCard(
            digitalTwinState = digitalTwinState
        )

        ScreenSectionSpacer()

        WellbeingAssessmentCard(
            wellbeingAssessment = wellbeingAssessment
        )

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

        ScreenFooter(
            lastAction = lastAction,
            debugLines = debugLines
        )
    }
}
