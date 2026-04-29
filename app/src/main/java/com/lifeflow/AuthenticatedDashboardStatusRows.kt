package com.lifeflow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

private val DashboardStatusLineGap = 4.dp
private val DashboardStatusLabelWidth = 72.dp

@Composable
internal fun DashboardStatusRows(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
) {
    DashboardValueLine("Health", healthStateDisplayLabel(healthState), healthStateColor(healthState))
    Spacer(modifier = Modifier.height(DashboardStatusLineGap))
    DashboardValueLine("Access", accessDisplayLabel(requiredCount, grantedCount), permissionCoverageColor(requiredCount, grantedCount))
    Spacer(modifier = Modifier.height(DashboardStatusLineGap))
    DashboardValueLine("Movement", grantedLabel(stepsGranted), grantedStateColor(stepsGranted))
    Spacer(modifier = Modifier.height(DashboardStatusLineGap))
    DashboardValueLine("Heart", grantedLabel(hrGranted), grantedStateColor(hrGranted))
}

@Composable
private fun DashboardValueLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = lifeFlowCardRowLabelStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(DashboardStatusLabelWidth)
        )
        Text(
            text = value,
            style = lifeFlowCardRowValueStyle(),
            color = valueColor
        )
    }
}

private fun healthStateDisplayLabel(
    healthState: HealthConnectUiState
): String {
    return when (healthState) {
        HealthConnectUiState.Unknown -> "Checking"
        HealthConnectUiState.Available -> "Ready"
        HealthConnectUiState.NotInstalled -> "Needs setup"
        HealthConnectUiState.NotSupported -> "Unavailable"
        HealthConnectUiState.UpdateRequired -> "Update needed"
    }
}

private fun accessDisplayLabel(
    requiredCount: Int,
    grantedCount: Int
): String {
    return if (requiredCount > 0 && grantedCount >= requiredCount) "Ready" else "$grantedCount / $requiredCount ready"
}

private fun grantedLabel(granted: Boolean): String {
    return if (granted) "Ready" else "Missing"
}
