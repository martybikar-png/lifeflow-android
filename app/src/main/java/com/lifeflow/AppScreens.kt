package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
fun LoadingScreen(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    lastAction: String,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "LifeFlow") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.lifeflow_one_icon),
                contentDescription = "LifeFlow One icon",
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = loadingMessage(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        GuidanceCard(
            title = "Current focus",
            message = loadingGuidanceMessage(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(title = "Next actions") {
            Button(
                onClick = onAuthenticate,
                enabled = !isAuthenticating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isAuthenticating) "Authenticating..." else "Authenticate")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGrantHealthPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Health permissions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onOpenHealthConnectSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Health Connect settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)

        Spacer(modifier = Modifier.height(16.dp))

        DebugCard(debugLines = debugLines)
    }
}

@Composable
fun DashboardScreen(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    digitalTwinState: DigitalTwinState?,
    lastAction: String,
    onRefreshNow: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onReAuthenticate: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "LifeFlow Dashboard") {
        DashboardStatusCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            digitalTwinState = digitalTwinState
        )

        Spacer(modifier = Modifier.height(16.dp))

        GuidanceCard(
            title = "Next best step",
            message = dashboardGuidanceMessage(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        Spacer(modifier = Modifier.height(16.dp))

        DigitalTwinCard(digitalTwinState = digitalTwinState)

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(title = "Actions") {
            Button(
                onClick = onRefreshNow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh now")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGrantHealthPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Health permissions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onOpenHealthConnectSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Health Connect settings")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReAuthenticate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Re-authenticate")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)

        Spacer(modifier = Modifier.height(16.dp))

        DebugCard(debugLines = debugLines)
    }
}

@Composable
fun ErrorScreen(
    message: String,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    lastAction: String,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onResetVault: () -> Unit,
    debugLines: List<String>,
    showAuthenticateAction: Boolean,
    showResetVaultAction: Boolean
) {
    ScreenContainer(title = "LifeFlow Error") {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GuidanceCard(
            title = "Recovery guidance",
            message = errorGuidanceMessage(
                message = message,
                showAuthenticateAction = showAuthenticateAction,
                showResetVaultAction = showResetVaultAction
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(title = "Recovery actions") {
            if (showAuthenticateAction) {
                Button(
                    onClick = onAuthenticate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Authenticate again")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onGrantHealthPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Health permissions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onOpenHealthConnectSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Health Connect settings")
            }

            if (showResetVaultAction) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onResetVault,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset vault")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)

        Spacer(modifier = Modifier.height(16.dp))

        DebugCard(debugLines = debugLines)
    }
}

@Composable
private fun GuidanceCard(
    title: String,
    message: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun loadingGuidanceMessage(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        isAuthenticating ->
            "Authentication is already active. As soon as the current checks finish, the protected dashboard can continue."

        healthState == HealthConnectUiState.NotInstalled ->
            "Install Health Connect first, then return here and authenticate again."

        healthState == HealthConnectUiState.UpdateRequired ->
            "Update Health Connect first, then come back and continue with authentication."

        requiredCount > 0 && grantedCount < requiredCount ->
            "You can authenticate now, but granting the missing permissions will unlock a fuller wellbeing snapshot."

        else ->
            "Authenticate to unlock the protected dashboard and load the first Digital Twin snapshot."
    }
}

private fun dashboardGuidanceMessage(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Open Health Connect settings first. Until Health Connect is ready, the dashboard cannot expose full wellbeing data."

        digitalTwinState == null ->
            "Tap Refresh now to load the first Digital Twin snapshot."

        requiredCount == 0 ->
            "The wellbeing permission contract is still pending. Refresh again after Health Connect resolves the available permissions."

        requiredCount > 0 && grantedCount < requiredCount ->
            "Grant the remaining permissions to improve snapshot coverage and unblock missing signals."

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Some signals are blocked by permissions. Open Health Connect settings and grant access to complete the snapshot."

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
            digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "The dashboard is working, but there is not enough usable wellbeing data yet. Refresh again later after more data is collected."

        else ->
            "The dashboard is ready. Use Refresh now whenever you want a newer wellbeing snapshot."
    }
}

private fun errorGuidanceMessage(
    message: String,
    showAuthenticateAction: Boolean,
    showResetVaultAction: Boolean
): String {
    return when {
        showResetVaultAction ->
            "This is a security-critical path. Reset the vault first, then authenticate again to rebuild a trusted state."

        message.contains("session expired", ignoreCase = true) ->
            "Your protected session expired. Authenticate again to continue."

        message.contains("security degraded", ignoreCase = true) ->
            "The trust state was downgraded. Authenticate again before protected flows continue."

        showAuthenticateAction ->
            "Try authenticating again first. If Health Connect still looks limited, open settings and retry."

        else ->
            "Review access and permissions in settings, then try recovery again."
    }
}