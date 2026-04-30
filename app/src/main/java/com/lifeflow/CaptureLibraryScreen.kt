package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CaptureLibraryScreen(
    onBackToQuickCapture: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Capture Library",
        screenSubtitle = "Review light captures.",
        infoTitle = "Library",
        infoBody = "Light captures appear here.",
        infoMarkers = listOf("Saved", "Light", "Clear"),
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Back to Capture",
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
