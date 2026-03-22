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
        GuidanceCard(
            title = "Preparing the shell calmly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This loading layer keeps the next step clear and proportional while LifeFlow checks access, session state, and health surfaces."
        )
        ScreenSectionSpacer()
        ActionCard(title = "Current state") {
            Text(
                text = loadingMessage(
                    isAuthenticating = isAuthenticating,
                    healthState = healthState,
                    requiredCount = requiredCount,
                    grantedCount = grantedCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenSectionSpacer()
        GuidanceCard(
            title = "Current focus",
            leadingIconResId = R.drawable.lf_ic_focus,
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
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
fun FreeTierScreen(
    message: String,
    lastAction: String,
    onUpgradeToCore: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "LifeFlow Free") {
        LoadingHeader()
        ScreenSectionSpacer()
        GuidanceCard(
            title = "Free mode, explained clearly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This screen explains the current product tier in a calm way. It should feel informative and low-pressure, not like a hard sales wall."
        )
        ScreenSectionSpacer()
        ActionCard(title = "Current tier state") {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenSectionSpacer()
        ActionCard(title = "What Core opens") {
            Text(
                text = "Core opens the Digital Twin, biometric vault, broader module access, and deeper cross-module intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScreenSectionSpacer()
            Button(
                onClick = onUpgradeToCore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Core")
            }
        }
        ScreenSectionSpacer()
        ActionCard(title = "Current boundary") {
            Text(
                text = "This is a tier-state screen only. It does not define trust-state truth, biometric authority, or protected execution behavior.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
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
        GuidanceCard(
            title = if (resetRequired) "Recovery needs a stronger step" else "Something needs attention",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This screen explains the current state and offers the next safe action without turning the moment into a harsh system wall."
        )
        ScreenSectionSpacer()
        ActionCard(title = "Current state") {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenSectionSpacer()
        ActionCard(title = "Recommended next step") {
            Text(
                text = if (resetRequired) {
                    "A vault reset is required before continuing safely."
                } else {
                    "You can retry after reviewing the current state."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScreenSectionSpacer()
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (resetRequired) "Reset vault" else "Try again")
            }
        }
        ScreenSectionSpacer()
        ActionCard(title = "Current boundary") {
            Text(
                text = "This screen surfaces the current state and the next safe action. It does not redefine trust-state truth or protected rules in UI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
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
