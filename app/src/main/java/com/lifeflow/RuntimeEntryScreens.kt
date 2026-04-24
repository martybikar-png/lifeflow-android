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
    val visibleMessage = message.ifBlank { "Free mode is active." }

    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Free mode")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowCardShell(
            title = "Free mode",
            summary = visibleMessage
        ) {
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

    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(
                text = if (resetRequired) "Reset" else "Recovery"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowCardShell(
            title = content.guidanceTitle,
            summary = content.guidanceMessage
        ) {
            Text(
                text = content.nextStepMessage,
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