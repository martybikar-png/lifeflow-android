package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CaptureEntryScreen(
    onBackToQuickCapture: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Capture Entry",
        screenSubtitle = "Finish a simple capture.",
        infoTitle = "New capture",
        infoBody = "Capture entry is ready."
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Done",
                onClick = onBackToQuickCapture,
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBackToQuickCapture,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
