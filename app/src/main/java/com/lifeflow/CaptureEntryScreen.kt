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
    onSaveCapture: (String) -> Unit = {},
    statusMessage: String = "",
    onBackToQuickCapture: () -> Unit = {},
) {
    var note by rememberSaveable { mutableStateOf("") }

    PublicShellInfoActionScreen(
        screenTitle = "Capture Entry",
        screenSubtitle = "Finish a simple capture.",
        infoTitle = "New capture",
        infoBody = "Add one short note.",
        infoMarkers = listOf("Simple", "Draft", "Done"),
        infoNote = captureInfoNote(statusMessage = statusMessage),
    ) {
        PublicShellActionPanel {
            LifeFlowSoftTextInput(
                value = note,
                onValueChange = { note = it },
                placeholder = "Add a note",
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowPrimaryActionButton(
                label = "Done",
                onClick = {
                    onSaveCapture(note)
                    onBackToQuickCapture()
                },
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
