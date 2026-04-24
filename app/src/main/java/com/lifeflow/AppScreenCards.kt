package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun DashboardStatusCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
) {
    val compactSummary = when {
        healthState != HealthConnectUiState.Available ->
            "Dashboard is limited."
        digitalTwinState == null ->
            "First snapshot is pending."
        hasMissingHealthPermissions(requiredCount, grantedCount) ->
            "Dashboard is partly ready."
        else ->
            "Dashboard is ready."
    }

    StatusCardShell(
        title = "Dashboard",
        titleColor = MaterialTheme.colorScheme.primary,
        summary = compactSummary
    ) {
        KeyValueStatusLine(
            label = "Health",
            value = permissionCoverageLabel(
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            valueColor = permissionCoverageColor(
                requiredCount = requiredCount,
                grantedCount = grantedCount
            )
        )
        KeyValueStatusLine(
            label = "Twin",
            value = digitalTwinReadinessLabel(digitalTwinState),
            valueColor = digitalTwinReadinessColor(digitalTwinState)
        )
        KeyValueStatusLine(
            label = "Next",
            value = dashboardNextMoveLabel(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            ),
            valueColor = dashboardNextMoveColor(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            )
        )
    }
}

@Composable
internal fun HealthSummaryCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Health Connect view")
        }

        LifeFlowSectionPanel(title = "Health Connect") {
            Text(
                text = healthCompactSummary(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LifeFlowSectionPanel(title = "Health state") {
            KeyValueStatusLine(
                label = "State",
                value = healthStateLabel(healthState),
                valueColor = healthStateColor(healthState)
            )
            KeyValueStatusLine(
                label = "Access",
                value = permissionCoverageLabel(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                valueColor = permissionCoverageColor(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueStatusLine(
                label = "Next",
                value = healthNextMoveLabel(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                valueColor = healthNextMoveColor(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
        }

        LifeFlowSectionPanel(title = "Signal bars") {
            HealthSignalBars(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                stepsGranted = stepsGranted,
                hrGranted = hrGranted
            )
        }

        LifeFlowSectionPanel(title = "Health details") {
            KeyValueLine("Required", requiredCount.toString())
            KeyValueLine("Granted", grantedCount.toString())
            KeyValueStatusLine(
                label = "Steps",
                value = if (stepsGranted) "Granted" else "Missing",
                valueColor = grantedStateColor(stepsGranted)
            )
            KeyValueStatusLine(
                label = "Heart",
                value = if (hrGranted) "Granted" else "Missing",
                valueColor = grantedStateColor(hrGranted)
            )
        }
    }
}

private fun healthCompactSummary(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Health Connect needs attention."
        hasMissingHealthPermissions(requiredCount, grantedCount) ->
            "Health access is incomplete."
        else ->
            "Health access is ready."
    }
}

@Composable
internal fun StatusCardShell(
    title: String,
    titleColor: Color,
    summary: String,
    leadingIconResId: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeFlowCardShell(
        title = title,
        titleColor = titleColor,
        summary = summary,
        leadingIconResId = leadingIconResId,
        content = content
    )
}