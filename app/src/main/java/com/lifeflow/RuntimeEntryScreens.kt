package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
    ProtectedLoginScreen(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount,
        onAuthenticate = onAuthenticate,
        onGrantHealthPermissions = onGrantHealthPermissions,
        onOpenHealthConnectSettings = onOpenHealthConnectSettings
    )
}

@Composable
fun FreeTierScreen(
    message: String,
    onUpgradeToCore: () -> Unit
) {
    val visibleMessage = message.ifBlank { "Free mode is active." }

    PublicShellInfoActionScreen(
        screenTitle = "Free",
        screenSubtitle = "Core stays protected.",
        infoTitle = "Free mode",
        infoBody = visibleMessage,
        infoNote = "Core features stay locked until you upgrade.",
        actionTopGap = 208.dp,
        showGoldEdge = true
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Upgrade to Core",
                onClick = onUpgradeToCore,
                modifier = Modifier.fillMaxWidth()
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

    PublicShellInfoActionScreen(
        screenTitle = if (resetRequired) "Reset" else "Recovery",
        screenSubtitle = if (resetRequired) "Protected reset needed." else "Safe recovery path.",
        infoTitle = content.guidanceTitle,
        infoBody = content.guidanceMessage,
        infoNote = content.nextStepMessage,
        actionTopGap = 208.dp,
        showGoldEdge = true
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = content.buttonLabel,
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
