package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
                label = "Permission coverage",
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
                label = "Permission coverage",
                value = permissionCoverageLabel(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                valueColor = permissionCoverageColor(
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                )
            )
            KeyValueLine("Required permissions", requiredCount.toString())
            KeyValueLine("Granted permissions", grantedCount.toString())
            KeyValueStatusLine(
                label = "Steps permission",
                value = if (stepsGranted) "Granted" else "Not granted",
                valueColor = grantedStateColor(stepsGranted)
            )
            KeyValueStatusLine(
                label = "Heart rate permission",
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

@Composable
private fun KeyValueStatusLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
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

@Composable
private fun healthReadinessColor(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        healthState == HealthConnectUiState.NotInstalled ||
            healthState == HealthConnectUiState.NotSupported ||
            healthState == HealthConnectUiState.UpdateRequired ->
            colors.error

        healthState == HealthConnectUiState.Available &&
            requiredCount > 0 &&
            grantedCount >= requiredCount ->
            colors.primary

        healthState == HealthConnectUiState.Available &&
            requiredCount == 0 ->
            colors.tertiary

        healthState == HealthConnectUiState.Available &&
            grantedCount < requiredCount ->
            colors.tertiary

        else ->
            colors.onSurfaceVariant
    }
}

@Composable
private fun permissionCoverageColor(
    requiredCount: Int,
    grantedCount: Int
): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        requiredCount == 0 -> colors.tertiary
        grantedCount >= requiredCount -> colors.primary
        else -> colors.tertiary
    }
}

@Composable
private fun digitalTwinReadinessColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.onSurfaceVariant
    }

    return when {
        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK ->
            colors.primary

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.stepsAvailability == DigitalTwinState.Availability.BLOCKED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.BLOCKED ->
            colors.error

        else ->
            colors.tertiary
    }
}

@Composable
private fun digitalTwinSignalCoverageColor(
    digitalTwinState: DigitalTwinState?
): Color {
    val colors = MaterialTheme.colorScheme

    if (digitalTwinState == null) {
        return colors.onSurfaceVariant
    }

    var availableSignals = 0
    if (digitalTwinState.stepsAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }
    if (digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.OK) {
        availableSignals++
    }

    return when (availableSignals) {
        2 -> colors.primary
        1 -> colors.tertiary
        else -> colors.onSurfaceVariant
    }
}

@Composable
private fun grantedStateColor(granted: Boolean): Color {
    return if (granted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun yesNoColor(value: Boolean): Color {
    return if (value) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun availabilityColor(
    availability: DigitalTwinState.Availability
): Color {
    val colors = MaterialTheme.colorScheme

    return when (availability) {
        DigitalTwinState.Availability.OK -> colors.primary
        DigitalTwinState.Availability.PERMISSION_DENIED -> colors.error
        DigitalTwinState.Availability.BLOCKED -> colors.error
        DigitalTwinState.Availability.NO_DATA -> colors.tertiary
        DigitalTwinState.Availability.UNKNOWN -> colors.onSurfaceVariant
    }
}