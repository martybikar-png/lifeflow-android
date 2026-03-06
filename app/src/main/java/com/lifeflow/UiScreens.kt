package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    debugLines: List<String>? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LifeFlow", style = MaterialTheme.typography.headlineMedium)

        if (isAuthenticating) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Authenticating…", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator()
                        Spacer(Modifier.width(12.dp))
                        Text("Please wait", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Locked", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Authenticate to initialize encrypted identity.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onAuthenticate) {
                        Text("Authenticate")
                    }
                }
            }
        }

        HealthConnectCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted,
            lastAction = lastAction,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        DebugCard(debugLines)
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
    onResetVault: (() -> Unit)? = null,
    debugLines: List<String>? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Security Error", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onAuthenticate) { Text("Authenticate") }
                    if (onResetVault != null) {
                        OutlinedButton(onClick = onResetVault) { Text("Reset Vault") }
                    }
                }
            }
        }

        HealthConnectCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted,
            lastAction = lastAction,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        DebugCard(debugLines)
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
    debugLines: List<String>? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Encrypted identity loaded.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onReAuthenticate) { Text("Re-authenticate") }
                    OutlinedButton(onClick = onRefreshNow) { Text("Refresh now") }
                }
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Digital Twin", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (digitalTwinState == null) {
                    Text("Digital Twin not initialized.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Identity initialized: ${digitalTwinState.identityInitialized}")
                    Spacer(Modifier.height(6.dp))
                    Text("Steps (24h): ${digitalTwinState.stepsLast24h ?: "—"}")
                    Spacer(Modifier.height(6.dp))
                    Text("Avg HR (24h): ${digitalTwinState.avgHeartRateLast24h ?: "—"}")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Availability: steps=${digitalTwinState.stepsAvailability.name}, hr=${digitalTwinState.heartRateAvailability.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Notes: ${digitalTwinState.notes.take(3).joinToString(" | ").ifEmpty { "—" }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Last update: ${formatEpochMillis(digitalTwinState.lastUpdatedEpochMillis)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        HealthConnectCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted,
            lastAction = lastAction,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        DebugCard(debugLines)
    }
}

@Composable
private fun HealthConnectCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    lastAction: String,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Health Connect", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text("Status: ${healthStateLabel(healthState)}")
            Spacer(Modifier.height(6.dp))
            Text(
                "Required: $requiredCount • Granted: $grantedCount",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Steps permission: ${if (stepsGranted) "Granted" else "Denied"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "HR permission: ${if (hrGranted) "Granted" else "Denied"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))
            Text("Last action: $lastAction", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onGrantHealthPermissions) { Text("Grant permissions") }
                OutlinedButton(onClick = onOpenHealthConnectSettings) { Text("Open settings") }
            }
        }
    }
}

@Composable
private fun DebugCard(debugLines: List<String>?) {
    if (debugLines.isNullOrEmpty()) return

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Debug", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            debugLines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun healthStateLabel(state: HealthConnectUiState): String =
    when (state) {
        HealthConnectUiState.Unknown -> "Unknown"
        HealthConnectUiState.Available -> "Available"
        HealthConnectUiState.NotInstalled -> "Not installed"
        HealthConnectUiState.NotSupported -> "Not supported"
        HealthConnectUiState.UpdateRequired -> "Update required"
    }

private fun formatEpochMillis(epochMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}