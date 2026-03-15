package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun LoadingActionsCard(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    ActionCard(title = "Recommended actions") {
        Text(
            text = loadingActionHint(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        ActionSectionSpacer()

        AuthenticateButton(
            isAuthenticating = isAuthenticating,
            onAuthenticate = onAuthenticate
        )

        ActionButtonSpacer()

        HealthPermissionsButton(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onGrantHealthPermissions = onGrantHealthPermissions
        )

        ActionButtonSpacer()

        OpenHealthConnectSettingsButton(
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )
    }
}

@Composable
internal fun DashboardActionsCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?,
    onRefreshNow: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onReAuthenticate: () -> Unit
) {
    ActionCard(title = "Dashboard actions") {
        Text(
            text = dashboardActionHint(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        ActionSectionSpacer()

        RefreshDashboardButton(
            healthState = healthState,
            digitalTwinState = digitalTwinState,
            onRefreshNow = onRefreshNow
        )

        ActionButtonSpacer()

        HealthPermissionsButton(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onGrantHealthPermissions = onGrantHealthPermissions
        )

        ActionButtonSpacer()

        OpenHealthConnectSettingsButton(
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        ActionButtonSpacer()

        ReAuthenticateButton(
            onReAuthenticate = onReAuthenticate
        )
    }
}

@Composable
internal fun ScreenFooter(
    lastAction: String,
    debugLines: List<String>
) {
    ScreenSectionSpacer()
    LastActionCard(lastAction = lastAction)

    ScreenSectionSpacer()
    DebugCard(debugLines = debugLines)
}

@Composable
internal fun ScreenSectionSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun ActionSectionSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ActionButtonSpacer() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun AuthenticateButton(
    isAuthenticating: Boolean,
    onAuthenticate: () -> Unit
) {
    Button(
        onClick = onAuthenticate,
        enabled = !isAuthenticating,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isAuthenticating) "Authenticating..." else "Authenticate")
    }
}

@Composable
private fun RefreshDashboardButton(
    healthState: HealthConnectUiState,
    digitalTwinState: DigitalTwinState?,
    onRefreshNow: () -> Unit
) {
    Button(
        onClick = onRefreshNow,
        enabled = canRefreshDashboard(healthState),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (digitalTwinState == null) {
                "Load first snapshot"
            } else {
                "Refresh now"
            }
        )
    }
}

@Composable
private fun HealthPermissionsButton(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onGrantHealthPermissions: () -> Unit
) {
    OutlinedButton(
        onClick = onGrantHealthPermissions,
        enabled = canGrantHealthPermissions(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (hasMissingHealthPermissions(requiredCount, grantedCount)) {
                "Review health access"
            } else {
                "Health access ready"
            }
        )
    }
}

@Composable
private fun OpenHealthConnectSettingsButton(
    onOpenHealthConnectSettings: () -> Unit
) {
    OutlinedButton(
        onClick = onOpenHealthConnectSettings,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Open Health Connect settings")
    }
}

@Composable
private fun ReAuthenticateButton(
    onReAuthenticate: () -> Unit
) {
    OutlinedButton(
        onClick = onReAuthenticate,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Authenticate again")
    }
}