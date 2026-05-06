package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val CaptureEntryActionPanelVerticalOffset = (-44).dp
private val CaptureEntryActionColumnGap = 16.dp

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
        PublicShellActionPanel(
            modifier = Modifier.offset(y = CaptureEntryActionPanelVerticalOffset)
        ) {
            LifeFlowSoftTextInput(
                value = note,
                onValueChange = { note = it },
                placeholder = "Write a note",
                modifier = Modifier.fillMaxWidth()
            )

            CaptureEntryActionRow {
                if (hasSubmittedCapture) {
                    LifeFlowHomePrimaryActionButton(
                        label = "Open Library",
                        onClick = onOpenCaptureLibrary,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LifeFlowHomePrimaryActionButton(
                        label = "Save",
                        onClick = {
                            val saveAction = onSaveCapture
                            if (saveAction != null) {
                                saveAction(note)
                                hasSubmittedCapture = true
                                note = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canSaveCapture
                    )
                }

                LifeFlowHomeSecondaryActionButton(
                    label = "Back",
                    onClick = onBackToQuickCapture,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CaptureEntryActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CaptureEntryActionColumnGap),
        content = content
    )
}
