package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val CaptureLibraryActionColumnGap = 16.dp
private val CaptureLibraryActionRowGap = 28.dp

@Composable
fun CaptureLibraryScreen(
    presentation: QuickCaptureLibraryPresentation = QuickCaptureLibraryPresentation.initial(),
    statusMessage: String = "",
    onLoadLibrary: () -> Unit = {},
    onDeleteCapture: (String) -> Unit = {},
    onUpdateCapture: (String, String) -> Unit = { _, _ -> },
    onBackToQuickCapture: () -> Unit = {},
) {
    var openedCaptureId by rememberSaveable { mutableStateOf<String?>(null) }
    var editCaptureId by rememberSaveable { mutableStateOf<String?>(null) }
    var editNote by rememberSaveable { mutableStateOf("") }

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
    val isEditing = editCaptureId != null && editCaptureId == detailCapture?.id
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
            editCaptureId = null
            editNote = ""
        }
    }

    LaunchedEffect(statusMessage) {
        when {
            statusMessage.trim().startsWith("Quick capture updated", ignoreCase = true) -> {
                editCaptureId = null
                editNote = ""
            }

            statusMessage.trim().startsWith("Quick capture deleted", ignoreCase = true) -> {
                openedCaptureId = null
                editCaptureId = null
                editNote = ""
            }
        }
    }

    PublicShellInfoActionScreen(
        screenTitle = if (isDetailOpen) "Capture Detail" else "Capture Library",
        screenSubtitle = if (isDetailOpen) detailPosition else "Review saved notes.",
        infoTitle = if (isDetailOpen) detailPosition else "Library",
        infoBody = if (isEditing) "Refine one note." else detailCapture?.note ?: presentation.infoBody,
        infoMarkers = if (isDetailOpen) {
            listOf(detailPosition, "Local", if (isEditing) "Editing" else "Calm")
        } else {
            presentation.markers
        },
        infoNote = captureInfoNote(statusMessage = statusMessage),
    ) {
        PublicShellActionPanel {
            val currentIndex = detailIndex
            val currentCapture = detailCapture

            if (currentIndex != null && currentCapture != null) {
                if (isEditing) {
                    LifeFlowSoftTextInput(
                        value = editNote,
                        onValueChange = { editNote = it },
                        placeholder = "Edit note",
                        modifier = Modifier.fillMaxWidth()
                    )

                    CaptureLibraryActionRow {
                        LifeFlowHomePrimaryActionButton(
                            label = "Save",
                            onClick = {
                                onUpdateCapture(currentCapture.id, editNote)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        LifeFlowHomeSecondaryActionButton(
                            label = "Cancel",
                            onClick = {
                                editCaptureId = null
                                editNote = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    if (
                        currentIndex < presentation.recentCaptures.lastIndex ||
                        currentIndex > 0
                    ) {
                        CaptureLibraryActionRow {
                            if (currentIndex < presentation.recentCaptures.lastIndex) {
                                LifeFlowHomeSecondaryActionButton(
                                    label = "Previous",
                                    onClick = {
                                        openedCaptureId =
                                            presentation.recentCaptures[currentIndex + 1].id
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            if (currentIndex > 0) {
                                LifeFlowHomeSecondaryActionButton(
                                    label = "Next",
                                    onClick = {
                                        openedCaptureId =
                                            presentation.recentCaptures[currentIndex - 1].id
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.height(CaptureLibraryActionRowGap))
                    }

                    CaptureLibraryActionRow {
                        LifeFlowHomePrimaryActionButton(
                            label = "Edit",
                            onClick = {
                                editCaptureId = currentCapture.id
                                editNote = currentCapture.note
                            },
                            modifier = Modifier.weight(1f)
                        )

                        LifeFlowHomeSecondaryActionButton(
                            label = "Delete",
                            onClick = {
                                onDeleteCapture(currentCapture.id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CaptureLibraryActionRowGap))

                CaptureLibraryCenteredAction {
                    LifeFlowHomeSecondaryActionButton(
                        label = "Back",
                        onClick = {
                            openedCaptureId = null
                            editCaptureId = null
                        }
                    )
                }
            } else {
                if (latestCapture != null) {
                    CaptureLibraryActionRow {
                        LifeFlowHomePrimaryActionButton(
                            label = "Open latest",
                            onClick = { openedCaptureId = latestCapture.id },
                            modifier = Modifier.weight(1f)
                        )

                        LifeFlowHomeSecondaryActionButton(
                            label = "Back",
                            onClick = onBackToQuickCapture,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    CaptureLibraryCenteredAction {
                        LifeFlowHomeSecondaryActionButton(
                            label = "Back",
                            onClick = onBackToQuickCapture
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureLibraryActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CaptureLibraryActionColumnGap),
        content = content
    )
}

@Composable
private fun CaptureLibraryCenteredAction(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        content()
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
