package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun DigitalTwinCard(
    digitalTwinState: DigitalTwinState?
) {
    StatusCardShell(
        title = "Digital Twin",
        titleColor = digitalTwinReadinessColor(digitalTwinState),
        summary = digitalTwinSummaryMessage(digitalTwinState)
    ) {
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
            KeyValueLine("Steps (24h)", digitalTwinState.stepsLast24h?.toString() ?: "—")
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
            KeyValueLine("Notes count", digitalTwinState.notes.size.toString())

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