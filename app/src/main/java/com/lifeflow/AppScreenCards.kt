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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dashboard status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dashboardSummaryMessage(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount,
                    digitalTwinState = digitalTwinState
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

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
}

@Composable
internal fun HealthSummaryCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Health Connect",
                style = MaterialTheme.typography.titleMedium,
                color = healthReadinessColor(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = healthSummaryMessage(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueStatusLine(
                label = "State",
                value = healthStateLabel(healthState),
                valueColor = healthReadinessColor(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
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
}

@Composable
internal fun DigitalTwinCard(
    digitalTwinState: DigitalTwinState?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Digital Twin",
                style = MaterialTheme.typography.titleMedium,
                color = digitalTwinReadinessColor(digitalTwinState)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = digitalTwinSummaryMessage(digitalTwinState),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueStatusLine(
                label = "Snapshot status",
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
                value = digitalTwinNextMoveLabel(digitalTwinState),
                valueColor = digitalTwinNextMoveColor(digitalTwinState)
            )

            if (digitalTwinState == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Digital Twin state available yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                KeyValueStatusLine(
                    label = "Identity initialized",
                    value = yesNoLabel(digitalTwinState.identityInitialized),
                    valueColor = yesNoColor(digitalTwinState.identityInitialized)
                )
                KeyValueLine(
                    "Steps (24h)",
                    digitalTwinState.stepsLast24h?.toString() ?: "—"
                )
                KeyValueLine(
                    "Avg heart rate (24h)",
                    digitalTwinState.avgHeartRateLast24h?.toString() ?: "—"
                )
                KeyValueStatusLine(
                    label = "Steps availability",
                    value = availabilityLabel(digitalTwinState.stepsAvailability),
                    valueColor = availabilityColor(digitalTwinState.stepsAvailability)
                )
                KeyValueStatusLine(
                    label = "Heart rate availability",
                    value = availabilityLabel(digitalTwinState.heartRateAvailability),
                    valueColor = availabilityColor(digitalTwinState.heartRateAvailability)
                )
                KeyValueLine(
                    "Last updated",
                    formatLastUpdated(digitalTwinState.lastUpdatedEpochMillis)
                )
                KeyValueLine(
                    "Notes count",
                    digitalTwinState.notes.size.toString()
                )

                if (digitalTwinState.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    digitalTwinState.notes.forEach { note ->
                        Text(
                            text = "• $note",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
