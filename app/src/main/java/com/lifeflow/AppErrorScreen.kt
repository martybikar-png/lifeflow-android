package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

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
            Text(
                text = errorActionHint(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount,
                    showAuthenticateAction = showAuthenticateAction,
                    showResetVaultAction = showResetVaultAction
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                enabled = canGrantHealthPermissions(
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (hasMissingHealthPermissions(requiredCount, grantedCount)) {
                        "Review Health access"
                    } else {
                        "Health access ready"
                    }
                )
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
