package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    StatusCardShell(
        title = "Dashboard status",
        titleColor = MaterialTheme.colorScheme.primary,
        summary = dashboardSummaryMessage(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            digitalTwinState = digitalTwinState
        )
    ) {
        KeyValueStatusLine(
            label = "Health readiness",
            value = healthReadinessLabel(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            valueColor = healthReadinessColor(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            )
        )
        KeyValueStatusLine(
            label = "Health access",
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
            label = "Twin snapshot",
            value = digitalTwinReadinessLabel(digitalTwinState),
            valueColor = digitalTwinReadinessColor(digitalTwinState)
        )
        KeyValueStatusLine(
            label = "Signal coverage",
            value = digitalTwinSignalCoverageLabel(digitalTwinState),
            valueColor = digitalTwinSignalCoverageColor(digitalTwinState)
        )
        KeyValueStatusLine(
            label = "Next move",
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
    StatusCardShell(
        title = "Health Connect",
        titleColor = MaterialTheme.colorScheme.primary,
        summary = healthSummaryMessage(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        )
    ) {
        KeyValueStatusLine(
            label = "State",
            value = healthStateLabel(healthState),
            valueColor = healthStateColor(healthState)
        )
        KeyValueStatusLine(
            label = "Readiness",
            value = healthReadinessLabel(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            valueColor = healthReadinessColor(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            )
        )
        KeyValueStatusLine(
            label = "Health access",
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
            label = "Next move",
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
        KeyValueLine("Required permissions", requiredCount.toString())
        KeyValueLine("Granted permissions", grantedCount.toString())
        KeyValueStatusLine(
            label = "Steps access",
            value = if (stepsGranted) "Granted" else "Not granted",
            valueColor = grantedStateColor(stepsGranted)
        )
        KeyValueStatusLine(
            label = "Heart rate access",
            value = if (hrGranted) "Granted" else "Not granted",
            valueColor = grantedStateColor(hrGranted)
        )
    }
}

@Composable
internal fun StatusCardShell(
    title: String,
    titleColor: Color,
    summary: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
