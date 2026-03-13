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
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dashboardSummaryMessage(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount,
                    digitalTwinState = digitalTwinState
                ),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueLine(
                "Health readiness",
                healthReadinessLabel(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueLine(
                "Permission coverage",
                permissionCoverageLabel(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueLine(
                "Twin snapshot",
                digitalTwinReadinessLabel(digitalTwinState)
            )
            KeyValueLine(
                "Signal coverage",
                digitalTwinSignalCoverageLabel(digitalTwinState)
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
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = healthSummaryMessage(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueLine("State", healthStateLabel(healthState))
            KeyValueLine(
                "Readiness",
                healthReadinessLabel(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueLine(
                "Permission coverage",
                permissionCoverageLabel(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueLine("Required permissions", requiredCount.toString())
            KeyValueLine("Granted permissions", grantedCount.toString())
            KeyValueLine("Steps permission", if (stepsGranted) "Granted" else "Not granted")
            KeyValueLine("Heart rate permission", if (hrGranted) "Granted" else "Not granted")
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
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = digitalTwinSummaryMessage(digitalTwinState),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueLine(
                "Snapshot status",
                digitalTwinReadinessLabel(digitalTwinState)
            )
            KeyValueLine(
                "Signal coverage",
                digitalTwinSignalCoverageLabel(digitalTwinState)
            )

            if (digitalTwinState == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Digital Twin state available yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                KeyValueLine(
                    "Identity initialized",
                    yesNoLabel(digitalTwinState.identityInitialized)
                )
                KeyValueLine(
                    "Steps (24h)",
                    digitalTwinState.stepsLast24h?.toString() ?: "—"
                )
                KeyValueLine(
                    "Avg heart rate (24h)",
                    digitalTwinState.avgHeartRateLast24h?.toString() ?: "—"
                )
                KeyValueLine(
                    "Steps availability",
                    availabilityLabel(digitalTwinState.stepsAvailability)
                )
                KeyValueLine(
                    "Heart rate availability",
                    availabilityLabel(digitalTwinState.heartRateAvailability)
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
                        style = MaterialTheme.typography.titleSmall
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

private fun healthReadinessLabel(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        healthState != HealthConnectUiState.Available -> healthStateLabel(healthState)
        requiredCount == 0 -> "Pending permission contract"
        grantedCount == 0 -> "No permissions granted"
        grantedCount < requiredCount -> "Partial access"
        else -> "Ready"
    }
}

private fun permissionCoverageLabel(
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        requiredCount == 0 -> "Pending"
        else -> "$grantedCount / $requiredCount"
    }
}

private fun digitalTwinReadinessLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "Not loaded yet"
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK ->
            "Complete"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Permission blocked"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            "Blocked"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "No data"

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.UNKNOWN &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.UNKNOWN ->
            "Unknown"

        else ->
            "Partial"
    }
}

private fun digitalTwinSignalCoverageLabel(
    digitalTwinState: DigitalTwinState?
): String {
    if (digitalTwinState == null) {
        return "0 / 2"
    }

    var availableSignals = 0
    if (digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }
    if (digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    return "$availableSignals / 2"
}