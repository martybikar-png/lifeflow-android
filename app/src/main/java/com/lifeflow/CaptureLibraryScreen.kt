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
    onDeleteCapture: (String) -> Unit = {},
    onBackToQuickCapture: () -> Unit = {},
) {
    var openedCaptureId by rememberSaveable { mutableStateOf<String?>(null) }
    val latestCapture = presentation.recentCaptures.firstOrNull()
    val captureCount = presentation.recentCaptures.size
    val detailIndex = openedCaptureId?.let { captureId ->
        presentation.recentCaptures
            .indexOfFirst { item -> item.id == captureId }
            .takeIf { index -> index >= 0 }
    }
    val detailCapture = detailIndex?.let { index ->
        presentation.recentCaptures[index]
    }
    val isDetailOpen = detailIndex != null && detailCapture != null
    val detailPosition = detailIndex?.let { index ->
        "Capture ${index + 1} of $captureCount"
    } ?: ""

    LaunchedEffect(Unit) {
        onLoadLibrary()
    }

    LaunchedEffect(presentation.recentCaptures) {
        val currentId = openedCaptureId
        if (
            currentId != null &&
            presentation.recentCaptures.none { item -> item.id == currentId }
        ) {
            openedCaptureId = null
        }
    }

    PublicShellInfoActionScreen(
        screenTitle = if (isDetailOpen) "Capture Detail" else "Capture Library",
        screenSubtitle = if (isDetailOpen) detailPosition else "Review light captures.",
        infoTitle = if (isDetailOpen) detailPosition else "Library",
        infoBody = detailCapture?.note ?: presentation.infoBody,
        infoMarkers = if (isDetailOpen) {
            listOf(detailPosition, "Local", "Calm")
        } else {
            presentation.markers
        },
        infoNote = captureInfoNote(statusMessage = statusMessage),
    ) {
        PublicShellActionPanel {
            val currentIndex = detailIndex
            val currentCapture = detailCapture

            if (currentIndex != null && currentCapture != null) {
                if (currentIndex < presentation.recentCaptures.lastIndex) {
                    LifeFlowSecondaryActionButton(
                        label = "Previous capture",
                        onClick = {
                            openedCaptureId =
                                presentation.recentCaptures[currentIndex + 1].id
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (currentIndex > 0) {
                    LifeFlowSecondaryActionButton(
                        label = "Next capture",
                        onClick = {
                            openedCaptureId =
                                presentation.recentCaptures[currentIndex - 1].id
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LifeFlowSecondaryActionButton(
                    label = "Delete capture",
                    onClick = {
                        openedCaptureId = null
                        onDeleteCapture(currentCapture.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                LifeFlowPrimaryActionButton(
                    label = "Back to Library",
                    onClick = { openedCaptureId = null },
                    modifier = Modifier.fillMaxWidth()
                )

                LifeFlowSecondaryActionButton(
                    label = "Back to Capture",
                    onClick = onBackToQuickCapture,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                if (latestCapture != null) {
                    LifeFlowPrimaryActionButton(
                        label = "Open latest",
                        onClick = { openedCaptureId = latestCapture.id },
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
