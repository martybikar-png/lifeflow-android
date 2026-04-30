package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.boundary.isLockedLike
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

private val DashboardHorizontalPadding = 20.dp
private val DashboardInfoTopGap = 44.dp
private val DashboardInfoBodyGap = 8.dp
private val DashboardActionHorizontalPadding = 12.dp

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
    onUpgradeToCore: () -> Unit,
    onOpenHome: () -> Unit,
    lastAction: String,
    isSessionAuthorized: Boolean
) {
    val dashboardState = resolveDashboardState(
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount,
        digitalTwinState = digitalTwinState,
        wellbeingAssessment = wellbeingAssessment
    )

    val hasLockedCoreSurface =
        boundarySnapshot.coreInsights.isLockedLike() ||
            boundarySnapshot.adaptiveHabits.isLockedLike()

    ScreenContainer(
        title = "LifeFlow Dashboard",
        subtitle = "Protected wellbeing overview.",
        showGoldEdge = true,
        scrollContent = false
    ) {
        LifeFlowActionFrame(
            modifier = Modifier.padding(horizontal = DashboardHorizontalPadding)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(DashboardInfoTopGap))

                Text(
                    text = dashboardTitle(dashboardState),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(DashboardInfoBodyGap))

                Text(
                    text = dashboardMessage(
                        dashboardState = dashboardState,
                        hasLockedCoreSurface = hasLockedCoreSurface
                    ),
                    style = lifeFlowCardSummaryStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(DashboardInfoBodyGap))

                Text(
                    text = "Protected signals are ready.",
                    style = lifeFlowCardRowLabelStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                DashboardStatusRows(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount,
                    stepsGranted = stepsGranted,
                    hrGranted = hrGranted
                )

                if (lastAction.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Snapshot refreshed.",
                        style = lifeFlowCardRowLabelStyle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LifeFlowBottomAnchoredActions(
                modifier = Modifier.padding(horizontal = DashboardActionHorizontalPadding)
            ) {
                LifeFlowPrimaryActionButton(
                    label = primaryDashboardActionLabel(
                        dashboardState = dashboardState,
                        isSessionAuthorized = isSessionAuthorized
                    ),
                    onClick = {
                        if (!isSessionAuthorized) {
                            onReAuthenticate()
                            return@LifeFlowPrimaryActionButton
                        }

                        when (dashboardState) {
                            DashboardState.HC_UNAVAILABLE -> onOpenHealthConnectSettings()
                            DashboardState.NEEDS_PERMISSIONS -> onGrantHealthPermissions()
                            DashboardState.LOADING,
                            DashboardState.NO_DATA,
                            DashboardState.ATTENTION,
                            DashboardState.READY -> onRefreshNow()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                LifeFlowSecondaryActionButton(
                    label = "Home",
                    onClick = onOpenHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun dashboardTitle(
    dashboardState: DashboardState
): String {
    return when (dashboardState) {
        DashboardState.HC_UNAVAILABLE -> "Health setup"
        DashboardState.NEEDS_PERMISSIONS -> "Almost there"
        DashboardState.LOADING -> "Preparing"
        DashboardState.NO_DATA -> "Ready to begin"
        DashboardState.ATTENTION -> "Needs attention"
        DashboardState.READY -> "Ready"
    }
}

private fun dashboardMessage(
    dashboardState: DashboardState,
    hasLockedCoreSurface: Boolean
): String {
    val baseMessage = when (dashboardState) {
        DashboardState.HC_UNAVAILABLE ->
            "Health Connect needs attention before the full dashboard can open."
        DashboardState.NEEDS_PERMISSIONS ->
            "Grant health access to unlock your wellbeing picture."
        DashboardState.LOADING ->
            "LifeFlow is preparing your protected snapshot."
        DashboardState.NO_DATA ->
            "Your protected snapshot is prepared."
        DashboardState.ATTENTION ->
            "Some protected signals need a fresh check."
        DashboardState.READY ->
            "Your protected dashboard is ready."
    }

    return if (hasLockedCoreSurface) {
        "$baseMessage Core surfaces stay protected."
    } else {
        baseMessage
    }
}

private fun primaryDashboardActionLabel(
    dashboardState: DashboardState,
    isSessionAuthorized: Boolean
): String {
    if (!isSessionAuthorized) {
        return "Authenticate again"
    }

    return when (dashboardState) {
        DashboardState.HC_UNAVAILABLE -> "Open Health Connect"
        DashboardState.NEEDS_PERMISSIONS -> "Review health access"
        DashboardState.LOADING,
        DashboardState.NO_DATA,
        DashboardState.ATTENTION,
        DashboardState.READY -> "Load snapshot"
    }
}
