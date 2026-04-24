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

@Composable
internal fun StartupFailureScreen(
    message: String,
    lastAction: String,
    onRetryStartup: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Recovery")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowCardShell(
            title = "Startup paused",
            summary = startupStatusLabel(message)
        ) {
            Text(
                text = startupRecoveryGuidance(message),
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = startupRecoveryActionHint(message),
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Retry startup",
                onClick = onRetryStartup
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowSecondaryActionButton(
                label = "Open App settings",
                onClick = onOpenAppSettings
            )

            if (lastAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastAction,
                    style = lifeFlowCardSummaryStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}