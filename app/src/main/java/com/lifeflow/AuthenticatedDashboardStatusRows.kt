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
        value = accessDisplayLabel(
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ),
        valueColor = permissionCoverageColor(
            requiredCount = requiredCount,
            grantedCount = grantedCount
        )
    )

    Spacer(modifier = Modifier.height(DashboardStatusLineGap))

    DashboardValueLine(
        label = "Movement",
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
    return if (requiredCount > 0 && grantedCount >= requiredCount) {
        "Ready"
    } else {
        "$grantedCount / $requiredCount ready"
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
