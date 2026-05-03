package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CaptureLibraryScreen(
    presentation: QuickCaptureLibraryPresentation = QuickCaptureLibraryPresentation.initial(),
    statusMessage: String = "",
    onLoadLibrary: () -> Unit = {},
    onBackToQuickCapture: () -> Unit = {},
) {
    var openedLatestNote by rememberSaveable { mutableStateOf<String?>(null) }
    val latestNote = presentation.recentNotes.firstOrNull()
    val detailNote = openedLatestNote
    val isDetailOpen = detailNote != null

    LaunchedEffect(Unit) {
        onLoadLibrary()
    }

    LaunchedEffect(latestNote) {
        if (openedLatestNote != null && openedLatestNote != latestNote) {
            openedLatestNote = null
        }
    }

    PublicShellInfoActionScreen(
        screenTitle = if (isDetailOpen) "Capture Detail" else "Capture Library",
        screenSubtitle = if (isDetailOpen) "Latest light capture." else "Review light captures.",
        infoTitle = if (isDetailOpen) "Latest capture" else "Library",
        infoBody = detailNote ?: presentation.infoBody,
        infoMarkers = if (isDetailOpen) {
            listOf("Latest", "Local", "Calm")
        } else {
            presentation.markers
        },
        infoNote = captureInfoNote(statusMessage = statusMessage),
    ) {
        PublicShellActionPanel {
            if (isDetailOpen) {
                LifeFlowPrimaryActionButton(
                    label = "Back to Library",
                    onClick = { openedLatestNote = null },
                    modifier = Modifier.fillMaxWidth()
                )

                LifeFlowSecondaryActionButton(
                    label = "Back to Capture",
                    onClick = onBackToQuickCapture,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                if (latestNote != null) {
                    LifeFlowPrimaryActionButton(
                        label = "Open latest",
                        onClick = { openedLatestNote = latestNote },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LifeFlowSecondaryActionButton(
                    label = "Back to Capture",
                    onClick = onBackToQuickCapture,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
