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
fun TrustScreen(
    lastAction: String = "Trust shell active",
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Trust") {
        GuidanceCard(
            title = "Trust, framed calmly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This screen explains trust as a product layer for orientation and future education. It is not the final source of security truth."
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
                    text = "It introduces the intended tone of trust in LifeFlow and gives lightweight orientation before any final trust-state logic or protected behavior is connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "What belongs here",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Calm trust overview\n• Clear language around state and access\n• Future trust education\n• Lightweight shell status framing",
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
                    text = "Trust should feel clear, stable, and proportional. The goal is to explain the layer without pretending that UI is the authority.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Settings shell")
                }
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
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
                    text = "This layer does not decide biometric authority, protected execution, recovery rules, or final trust-state branching. It stays informational and structural for now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}
