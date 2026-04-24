package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    val currentStateMessage = loadingMessage(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    ScreenContainer(title = "") {
        LoadingTransitionContent(
            isAuthenticating = isAuthenticating,
            currentStateMessage = currentStateMessage,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )
    }
}

@Composable
fun FreeTierScreen(
    message: String,
    onUpgradeToCore: () -> Unit
) {
    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Free mode")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Free mode is active") {
            Text(
                text = message,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            Text(
                text = "Upgrade to Core to unlock protected access.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Upgrade to Core",
                onClick = onUpgradeToCore
            )
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    resetRequired: Boolean,
    onRetry: () -> Unit
) {
    val content = resolveErrorScreenContent(
        message = message,
        resetRequired = resetRequired
    )

    val nextStepHint = if (resetRequired) {
        "Reset is required before protected access can continue."
    } else {
        "Authenticate again when you are ready."
    }

    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(
                text = if (resetRequired) "Reset" else "Recovery"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = content.guidanceTitle) {
            Text(
                text = message,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            Text(
                text = nextStepHint,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = content.buttonLabel,
                onClick = onRetry
            )
        }
    }
}
