package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        Text(
            text = if (isAuthenticating) {
                "Authentication session is active."
            } else {
                "Authentication is required before protected flows continue."
            },
            style = MaterialTheme.typography.bodyLarge
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Authenticate")
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
    debugLines: List<String>
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

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(title = "Recovery actions") {
            Button(
                onClick = onAuthenticate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Authenticate again")
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
                onClick = onResetVault,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset vault")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)

        Spacer(modifier = Modifier.height(16.dp))

        DebugCard(debugLines = debugLines)
    }
}

@Composable
private fun ScreenContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun HealthSummaryCard(
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

            Spacer(modifier = Modifier.height(12.dp))

            KeyValueLine("State", healthStateLabel(healthState))
            KeyValueLine("Required permissions", requiredCount.toString())
            KeyValueLine("Granted permissions", grantedCount.toString())
            KeyValueLine("Steps permission", if (stepsGranted) "Granted" else "Not granted")
            KeyValueLine("Heart rate permission", if (hrGranted) "Granted" else "Not granted")
        }
    }
}

@Composable
private fun DigitalTwinCard(
    digitalTwinState: DigitalTwinState?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Digital Twin",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (digitalTwinState == null) {
                Text(
                    text = "No Digital Twin state available yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                KeyValueLine(
                    "Identity initialized",
                    digitalTwinState.identityInitialized.toString()
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
                    digitalTwinState.stepsAvailability.name
                )
                KeyValueLine(
                    "Heart rate availability",
                    digitalTwinState.heartRateAvailability.name
                )
                KeyValueLine(
                    "Last updated",
                    digitalTwinState.lastUpdatedEpochMillis.toString()
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

@Composable
private fun LastActionCard(lastAction: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Last action",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastAction,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DebugCard(debugLines: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Debug",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (debugLines.isEmpty()) {
                Text(
                    text = "No debug lines.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                debugLines.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun KeyValueLine(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun healthStateLabel(state: HealthConnectUiState): String {
    return when (state) {
        HealthConnectUiState.Unknown -> "Unknown"
        HealthConnectUiState.Available -> "Available"
        HealthConnectUiState.NotInstalled -> "Not installed"
        HealthConnectUiState.NotSupported -> "Not supported"
        HealthConnectUiState.UpdateRequired -> "Update required"
    }
}