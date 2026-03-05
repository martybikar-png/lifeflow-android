package com.lifeflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Authenticating...")
    }
}

@Composable
fun DashboardScreen(
    healthState: HealthConnectUiState,
    stepsLast24h: Long?,
    avgHrLast24h: Long?,
    message: String?,
    digitalTwinState: DigitalTwinState?,
    onConnectHealth: () -> Unit,
    onReadSteps: () -> Unit,
    onReadHeartRate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Authentication Successful",
            style = MaterialTheme.typography.headlineMedium
        )

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LifeFlow Dashboard")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Encrypted identity loaded.")
            }
        }

        // 🧠 DIGITAL TWIN PANEL
        Card {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Digital Twin",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (digitalTwinState != null) {

                    Text("Identity initialized: ${digitalTwinState.identityInitialized}")
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Steps (24h): ${digitalTwinState.stepsLast24h ?: "—"}")
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Avg HR (24h): ${digitalTwinState.avgHeartRateLast24h ?: "—"}")
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Last update: ${formatEpochMillis(digitalTwinState.lastUpdatedEpochMillis)}")

                } else {
                    Text("Digital Twin not initialized.")
                }
            }
        }

        // ❤️ HEALTH CONNECT
        Card {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Health Connect",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Status: ${healthStateLabel(healthState)}")

                if (!message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onConnectHealth) {
                        Text("Connect Health")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onReadSteps) {
                        Text("Read Steps (24h)")
                    }

                    OutlinedButton(onClick = onReadHeartRate) {
                        Text("Read HR Avg (24h)")
                    }
                }

                if (stepsLast24h != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Steps (last 24h): $stepsLast24h")
                }

                if (avgHrLast24h != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Avg HR (last 24h): $avgHrLast24h bpm")
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Avg HR (last 24h): —")
                }
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

@Composable
fun ErrorScreen(
    message: String,
    onResetVault: (() -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Security Error",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        if (onResetVault != null) {

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onResetVault
            ) {
                Text("Reset Vault")
            }
        }
    }
}