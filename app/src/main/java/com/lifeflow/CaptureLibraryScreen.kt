package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun CaptureLibraryScreen(
    presentation: QuickCaptureLibraryPresentation = QuickCaptureLibraryPresentation.initial(),
    statusMessage: String = "",
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
        infoNote = captureInfoNote(statusMessage = statusMessage),
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
internal fun captureInfoNote(
    primaryNote: String = "",
    statusMessage: String = ""
): String {
    val parts = listOf(
        primaryNote.trim(),
        captureStatusMessageOrBlank(statusMessage)
    ).filter { it.isNotBlank() }

    return parts.joinToString(separator = "\n")
}

private fun captureStatusMessageOrBlank(message: String): String {
    val normalized = message.trim()
    if (normalized.isBlank()) return ""

    return when {
        normalized.startsWith("Quick capture", ignoreCase = true) -> normalized
        normalized.startsWith("Capture library", ignoreCase = true) -> normalized
        else -> ""
    }
}
