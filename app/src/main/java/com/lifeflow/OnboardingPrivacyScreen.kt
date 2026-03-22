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
fun OnboardingPrivacyScreen(
    lastAction: String = "Onboarding privacy shell active",
    onFinish: () -> Unit = {},
    onBack: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Privacy") {
        GuidanceCard(
            title = "Privacy, explained calmly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This step frames privacy as clear structure, understandable language, and respectful product boundaries."
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
                    text = "It explains the intended tone of privacy in LifeFlow before any final consent, biometric, or protected execution layers are connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "What privacy should feel like",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Clear boundaries\n• Calm explanations\n• Respect for sensitive flows\n• No pressure or hidden meaning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finish onboarding shell")
                }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
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
                    text = "This screen does not define final consent policy, trust-state truth, biometric authority, recovery behavior, or protected execution rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}
