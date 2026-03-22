package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    lastAction: String = "Settings shell active",
    onOpenPrivacy: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Settings") {
        GuidanceCard(
            title = "Calm app controls",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "Settings is a quiet shell for preferences, privacy framing, and future account surfaces. It is not connected to final protected behavior."
        )
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "What this step does",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "It gives the user a calm place to orient themselves around preferences, privacy entry points, and trust-related surfaces before any sensitive logic is connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "What belongs here",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• App preferences\n• Privacy entry points\n• Trust overview access\n• Future account and device controls",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "How this layer should feel",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Settings should feel quiet, clear, and easy to scan. The goal is structured orientation, not pressure or hidden system meaning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onOpenPrivacy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Privacy shell")
                }
                OutlinedButton(
                    onClick = onOpenTrust,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Trust shell")
                }
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Current boundary",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "No biometric authority, recovery policy, protected execution logic, or final trust-state branching is decided here. This layer stays informational and structural.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}
