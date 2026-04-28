package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TrustScreen(
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Trust",
        screenSubtitle = "Security and access.",
        infoTitle = "Trust",
        infoBody = "Security and access stay visible."
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Settings",
                onClick = onOpenSettings,
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
