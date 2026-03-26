package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun StartupFailureScreen(
    message: String,
    lastAction: String,
    onRetryStartup: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    ScreenContainer(title = "LifeFlow Startup") {
        GuidanceCard(
            title = "Startup needs a calmer recovery step",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This screen explains why startup could not complete and keeps the next action clear, safe, and proportional."
        )

        ScreenSectionSpacer()

        StartupFailureMessageCard(message = message)

        ScreenSectionSpacer()

        GuidanceCard(
            title = "What to do next",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = startupRecoveryGuidance(message)
        )

        ScreenSectionSpacer()

        ActionCard(title = "Recovery actions") {
            Text(
                text = startupRecoveryActionHint(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ScreenSectionSpacer()

            Button(
                onClick = onRetryStartup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry startup")
            }

            OutlinedButton(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open App settings")
            }
        }

        ScreenSectionSpacer()

        ActionCard(title = "Current boundary") {
            Text(
                text = "This startup screen surfaces the current state and the next safe action. It does not redefine trust-state truth or protected execution rules in UI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScreenSectionSpacer()

        LastActionCard(lastAction = lastAction)
    }
}

@Composable
private fun StartupFailureMessageCard(message: String) {
    ActionCard(title = "Current startup state") {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ScreenSectionSpacer()

        Text(
            text = "Startup status",
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = startupStatusLabel(message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
