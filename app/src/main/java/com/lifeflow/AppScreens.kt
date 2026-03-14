package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

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
                Text("Authenticate again")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)

        Spacer(modifier = Modifier.height(16.dp))

        DebugCard(debugLines = debugLines)
    }
}
