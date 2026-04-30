package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrivacyScreen(
    onOpenTrust: () -> Unit = {},
    onBackToSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Privacy",
        screenSubtitle = "Data boundaries stay clear.",
        infoTitle = "Privacy",
        infoBody = "Data boundaries stay clear.",
        infoMarkers = listOf("Local", "Clear", "Yours"),
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Trust",
                onClick = onOpenTrust,
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBackToSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
