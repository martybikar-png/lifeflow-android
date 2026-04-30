package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    onOpenPrivacy: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Settings",
        screenSubtitle = "Controls for privacy and trust.",
        infoTitle = "Controls",
        infoBody = "Privacy and trust controls.",
        infoMarkers = listOf("Privacy", "Trust", "Control"),
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Privacy",
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowSecondaryActionButton(
                label = "Trust",
                onClick = onOpenTrust,
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
