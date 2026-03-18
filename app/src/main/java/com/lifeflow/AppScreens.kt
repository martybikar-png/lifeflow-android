// app/src/main/java/com/lifeflow/AppScreens.kt
package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
        LoadingHeader()

        ScreenSectionSpacer()

        Text(
            text = loadingMessage(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        ScreenSectionSpacer()

        GuidanceCard(
            title = "Current focus",
            message = loadingGuidanceMessage(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            )
        )

        ScreenSectionSpacer()

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        ScreenSectionSpacer()

        LoadingActionsCard(
            isAuthenticating = isAuthenticating,
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        ScreenFooter(
            lastAction = lastAction,
            debugLines = debugLines
        )
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

        ScreenSectionSpacer()

        GuidanceCard(
            title = "Next best step",
            message = dashboardGuidanceMessage(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            )
        )

        ScreenSectionSpacer()

        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )

        ScreenSectionSpacer()

        DigitalTwinCard(digitalTwinState = digitalTwinState)

        ScreenSectionSpacer()

        DashboardActionsCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            digitalTwinState = digitalTwinState,
            onRefreshNow = onRefreshNow,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings,
            onReAuthenticate = onReAuthenticate
        )

        ScreenFooter(
            lastAction = lastAction,
            debugLines = debugLines
        )
    }
}

@Composable
fun ErrorScreen(
    message: String,
    resetRequired: Boolean,
    lastAction: String,
    onRetry: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "LifeFlow") {
        LoadingHeader()

        ScreenSectionSpacer()

        Text(
            text = if (resetRequired) "Vault reset required." else "Something needs your attention.",
            style = MaterialTheme.typography.headlineSmall
        )

        ScreenSectionSpacer()

        GuidanceCard(
            title = "Issue detected",
            message = message
        )

        ScreenSectionSpacer()

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (resetRequired) "Reset vault" else "Try again")
        }

        ScreenFooter(
            lastAction = lastAction,
            debugLines = debugLines
        )
    }
}

@Composable
private fun LoadingHeader() {
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
}