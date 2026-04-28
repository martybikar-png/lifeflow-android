package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun StartupFailureScreen(
    message: String,
    lastAction: String,
    onRetryStartup: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    PublicShellInfoActionScreen(
        screenTitle = "Recovery",
        screenSubtitle = "Startup paused.",
        infoTitle = "Startup paused",
        infoBody = startupRecoveryGuidance(message),
        infoNote = startupFailureInfoNote(
            message = message,
            lastAction = lastAction
        )
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Retry startup",
                onClick = onRetryStartup,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowSecondaryActionButton(
                label = "Open App settings",
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun startupFailureInfoNote(
    message: String,
    lastAction: String
): String {
    val actionHint = startupRecoveryActionHint(message)

    return if (lastAction.isBlank()) {
        actionHint
    } else {
        "$actionHint\n$lastAction"
    }
}
