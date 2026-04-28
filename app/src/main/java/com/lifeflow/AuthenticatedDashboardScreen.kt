package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
private val DashboardLineGap = 4.dp
private val DashboardActionTopGap = 92.dp
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
        showGoldEdge = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DashboardHorizontalPadding),
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
                text = "Tap Authenticate again if snapshot stays preparing.",
                style = lifeFlowCardRowLabelStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            DashboardValueLine(
                label = "Health",
                value = healthStateDisplayLabel(healthState),
                valueColor = healthStateValueColor(healthState)
            )

            Spacer(modifier = Modifier.height(DashboardLineGap))

            DashboardValueLine(
                label = "Access",
                value = "$grantedCount / $requiredCount",
                valueColor = accessValueColor(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )

            Spacer(modifier = Modifier.height(DashboardLineGap))

            DashboardValueLine(
                label = "Steps",
                value = grantedLabel(stepsGranted),
                valueColor = grantedValueColor(stepsGranted)
            )

            Spacer(modifier = Modifier.height(DashboardLineGap))

            DashboardValueLine(
                label = "Heart",
                value = grantedLabel(hrGranted),
                valueColor = grantedValueColor(hrGranted)
            )

            if (lastAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = lastAction,
                    style = lifeFlowCardRowLabelStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(DashboardActionTopGap))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DashboardActionHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
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

                Spacer(modifier = Modifier.height(8.dp))

                LifeFlowSecondaryActionButton(
                    label = "Authenticate again",
                    onClick = onReAuthenticate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DashboardValueLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = lifeFlowCardRowLabelStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = lifeFlowCardRowValueStyle(),
            color = valueColor
        )
    }
}

@Composable
private fun healthStateValueColor(
    healthState: HealthConnectUiState
): Color {
    return when (healthState) {
        HealthConnectUiState.Available -> MaterialTheme.colorScheme.primary
        HealthConnectUiState.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
        HealthConnectUiState.NotInstalled,
        HealthConnectUiState.NotSupported,
        HealthConnectUiState.UpdateRequired -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun accessValueColor(
    requiredCount: Int,
    grantedCount: Int
): Color {
    return when {
        requiredCount <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        grantedCount >= requiredCount -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun grantedValueColor(
    granted: Boolean
): Color {
    return if (granted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
}

private fun dashboardTitle(
    dashboardState: DashboardState
): String {
    return when (dashboardState) {
        DashboardState.HC_UNAVAILABLE -> "Health setup"
        DashboardState.NEEDS_PERMISSIONS -> "Almost there"
        DashboardState.LOADING -> "Preparing"
        DashboardState.NO_DATA -> "Ready to start"
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
            "Load your first snapshot to populate the dashboard."
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

private fun healthStateDisplayLabel(
    healthState: HealthConnectUiState
): String {
    return when (healthState) {
        HealthConnectUiState.Unknown -> "Unknown"
        HealthConnectUiState.Available -> "Available"
        HealthConnectUiState.NotInstalled -> "Not installed"
        HealthConnectUiState.NotSupported -> "Not supported"
        HealthConnectUiState.UpdateRequired -> "Update required"
    }
}

private fun grantedLabel(
    granted: Boolean
): String {
    return if (granted) {
        "Ready"
    } else {
        "Missing"
    }
}
