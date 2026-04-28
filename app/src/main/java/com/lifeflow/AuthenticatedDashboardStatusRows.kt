package com.lifeflow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

private val DashboardStatusLineGap = 4.dp

@Composable
internal fun DashboardStatusRows(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
) {
    DashboardValueLine(
        label = "Health",
        value = healthStateDisplayLabel(healthState),
        valueColor = healthStateColor(healthState)
    )

    Spacer(modifier = Modifier.height(DashboardStatusLineGap))

    DashboardValueLine(
        label = "Access",
        value = "$grantedCount / $requiredCount",
        valueColor = permissionCoverageColor(
            requiredCount = requiredCount,
            grantedCount = grantedCount
        )
    )

    Spacer(modifier = Modifier.height(DashboardStatusLineGap))

    DashboardValueLine(
        label = "Steps",
        value = grantedLabel(stepsGranted),
        valueColor = grantedStateColor(stepsGranted)
    )

    Spacer(modifier = Modifier.height(DashboardStatusLineGap))

    DashboardValueLine(
        label = "Heart",
        value = grantedLabel(hrGranted),
        valueColor = grantedStateColor(hrGranted)
    )
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
