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
fun HomeScreen(
    lastAction: String = "Home shell active",
    onOpenQuickCapture: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Home") {
        GuidanceCard(
            title = "One clear step first",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This home stays intentionally quiet. It gives you one main focal action first, then a small amount of supporting context."
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
                    text = "It provides a calm home surface with one clear next action and only a small amount of supporting orientation. This is structure first, not a final execution layer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "Primary focus",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Open Quick Capture to add something small without turning the home into a busy dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onOpenQuickCapture,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Quick Capture shell")
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
                    text = "Supporting surfaces",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Use Settings for calm app controls and Privacy entry points. Use Trust for lightweight orientation around future trust-related surfaces.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Settings shell")
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
                    text = "This is a shell-only home. No biometric truth, trust-state branching, recovery behavior, or protected execution is decided here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}
