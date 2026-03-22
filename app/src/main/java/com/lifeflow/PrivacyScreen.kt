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
fun PrivacyScreen(
    lastAction: String = "Privacy shell active",
    onOpenTrust: () -> Unit = {},
    onBackToSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Privacy") {
        GuidanceCard(
            title = "Privacy, explained softly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "Privacy is presented here as clear structure, understandable language, and respectful boundaries. This screen is not the final privacy engine."
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
                    text = "It explains the intended tone of privacy in LifeFlow and gives a calm orientation before deeper trust controls or sensitive flows are introduced.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "What belongs here",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Clear privacy framing\n• Simple explanations of sensitive areas\n• Calm boundaries around future flows\n• Orientation before deeper trust controls",
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
                    text = "Privacy should feel legible, calm, and proportionate. The goal is to reduce pressure, not increase it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onOpenTrust,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Trust shell")
                }
                OutlinedButton(
                    onClick = onBackToSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Settings")
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
                    text = "This shell does not define final consent policy, biometric authority, recovery behavior, or protected execution rules. It stays informational and structural in this phase.",
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
