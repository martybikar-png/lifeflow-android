package com.lifeflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.lifeflow.boundary.BoundaryPresentation
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.boundary.isLockedLike
import com.lifeflow.boundary.shouldShowUpgradeAction
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
    boundarySnapshot: MainBoundarySnapshot,
    onRefreshNow: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onReAuthenticate: () -> Unit,
    onUpgradeToCore: () -> Unit
) {
    val dashboardState = resolveDashboardState(
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount,
        digitalTwinState = digitalTwinState,
        wellbeingAssessment = wellbeingAssessment
    )

    val coreInsightsLocked = boundarySnapshot.coreInsights.isLockedLike()
    val adaptiveHabitsLocked = boundarySnapshot.adaptiveHabits.isLockedLike()

    val needsSetupActions = dashboardState in listOf(
        DashboardState.HC_UNAVAILABLE,
        DashboardState.NEEDS_PERMISSIONS
    )

    ScreenContainer(title = "LifeFlow Dashboard") {
        DashboardWelcomeCard(
            state = dashboardState,
            onPrimaryAction = {
                when (dashboardState) {
                    DashboardState.HC_UNAVAILABLE -> onOpenHealthConnectSettings()
                    DashboardState.NEEDS_PERMISSIONS -> onGrantHealthPermissions()
                    DashboardState.LOADING,
                    DashboardState.NO_DATA,
                    DashboardState.ATTENTION,
                    DashboardState.READY -> onRefreshNow()
                }
            }
        )

        ScreenSectionSpacer()

        if (needsSetupActions) {
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

        if (coreInsightsLocked) {
            BoundaryLockedTeaserCard(
                presentation = boundarySnapshot.coreInsights,
                onUpgradeToCore = onUpgradeToCore
            )
            ScreenSectionSpacer()
        } else if (wellbeingAssessment != null) {
            WellbeingAssessmentCard(
                wellbeingAssessment = wellbeingAssessment
            )
            ScreenSectionSpacer()
        }

        DigitalTwinCard(
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

        if (adaptiveHabitsLocked) {
            ScreenSectionSpacer()

            BoundaryLockedTeaserCard(
                presentation = boundarySnapshot.adaptiveHabits,
                onUpgradeToCore = onUpgradeToCore
            )
        } else if (!needsSetupActions) {
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
    }
}

@Composable
private fun BoundaryLockedTeaserCard(
    presentation: BoundaryPresentation,
    onUpgradeToCore: () -> Unit
) {
    LifeFlowSectionPanel(title = presentation.title) {
        Text(
            text = presentation.detailMessage,
            style = lifeFlowCardSummaryStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (presentation.shouldShowUpgradeAction()) {
            ScreenSectionSpacer()

            LifeFlowPrimaryActionButton(
                label = "Upgrade to Core",
                onClick = onUpgradeToCore
            )
        }
    }
}
