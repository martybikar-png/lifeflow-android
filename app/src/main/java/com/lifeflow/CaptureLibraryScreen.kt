package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun CaptureLibraryScreen(
    presentation: QuickCaptureLibraryPresentation = QuickCaptureLibraryPresentation.initial(),
    onLoadLibrary: () -> Unit = {},
    onBackToQuickCapture: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        onLoadLibrary()
    }

    PublicShellInfoActionScreen(
        screenTitle = "Capture Library",
        screenSubtitle = "Review light captures.",
        infoTitle = "Library",
        infoBody = presentation.infoBody,
        infoMarkers = presentation.markers,
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
