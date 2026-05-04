package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CaptureEntryScreen(
    onSaveCapture: ((String) -> Unit)? = null,
    onBackToQuickCapture: () -> Unit = {},
    onOpenCaptureLibrary: () -> Unit = {},
) {
    var note by rememberSaveable { mutableStateOf("") }
    var hasSubmittedCapture by rememberSaveable { mutableStateOf(false) }
    val canSaveCapture = onSaveCapture != null

    PublicShellInfoActionScreen(
        screenTitle = "Capture Entry",
        screenSubtitle = "Save one clear note.",
        infoTitle = "New capture",
        infoBody = "Write it down. Keep going.",
        infoMarkers = listOf("Simple", "Draft", "Save"),
        infoNote = captureInfoNote(
            primaryNote = if (hasSubmittedCapture) "Saved." else ""
        ),
    ) {
        PublicShellActionPanel {
            LifeFlowSoftTextInput(
                value = note,
                onValueChange = { note = it },
                placeholder = "Write a note",
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowPrimaryActionButton(
                label = "Save",
                onClick = {
                    val saveAction = onSaveCapture
                    if (saveAction != null) {
                        saveAction(note)
                        hasSubmittedCapture = true
                        note = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSaveCapture
            )

            if (hasSubmittedCapture) {
                LifeFlowSecondaryActionButton(
                    label = "Open Library",
                    onClick = onOpenCaptureLibrary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBackToQuickCapture,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
