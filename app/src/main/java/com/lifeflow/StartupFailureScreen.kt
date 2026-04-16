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
            LifeFlowSignalPill(text = "Permissions")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Startup needs a calmer recovery step") {
            Text(
                text = "Startup is paused at a visible recovery boundary. The safest next action is kept first.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Recovery actions") {
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Startup snapshot") {
            StartupFailureDetail(
                label = "Status",
                value = startupStatusLabel(message)
            )

            Spacer(modifier = Modifier.height(6.dp))

            StartupFailureDetail(
                label = "Next step",
                value = startupRecoveryGuidance(message)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Last action") {
            Text(
                text = lastAction.ifBlank { "No recorded startup action yet." },
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StartupFailureDetail(
    label: String,
    value: String
) {
    Text(
        text = label,
        style = lifeFlowCardRowLabelStyle(),
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = value,
        style = lifeFlowCardRowValueStyle(),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
