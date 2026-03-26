package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingPermissionsScreen(
    lastAction: String = "Onboarding permissions shell active",
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(
        title = "Permissions",
        showBackButton = true,
        onBack = onBack
    ) {
        GuidanceCard(
            title = "Explain first, request later",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This step explains why some features may ask for access later. It keeps the tone calm, proportional, and easy to understand."
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
                    text = "It explains the shape of future access in simple language before any real permission flow is connected. This is orientation first, not a final execution or authority layer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "Examples of future access",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Health Connect inputs\n• Lightweight wellbeing signals\n• Local snapshots for calmer home surfaces",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "What comes next",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Next, the privacy shell explains how LifeFlow wants to frame boundaries, consent tone, and sensitive product language.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to privacy")
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
                    text = "This screen does not launch the final permission flow, define trust-state truth, or decide protected execution behavior.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}
