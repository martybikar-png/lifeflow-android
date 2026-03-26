package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CaptureEntryScreen(
    lastAction: String = "Capture entry shell active",
    onBackToQuickCapture: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Capture Entry") {
        GuidanceCard(
            title = "Start one small capture",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "Capture Entry is the calm first step for starting a lightweight input. In this phase it stays a shell destination without final orchestration, storage, or protected execution."
        )
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureEntrySpacing)
            ) {
                Text(
                    text = "What this step does",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "It gives the main Quick Capture action its own destination, so the flow can move into a focused entry screen instead of stopping at feedback text only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "Entry focus",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "This shell can later host a lightweight note, short reflection, mood signal, or another small input. Right now it only defines the place where that future capture begins.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureEntrySpacing)
            ) {
                Text(
                    text = "Planned shell direction",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "• Quick note start\n• Reflection entry point\n• Lightweight signal input\n• Calm pre-orchestration layer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureEntrySpacing)
            ) {
                Text(
                    text = "Current boundary",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "This screen does not assert biometric authority, trust-state truth, protected storage, or final capture execution. It remains fully inside the current shell phase.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                OutlinedButton(
                    onClick = onBackToQuickCapture,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Quick Capture")
                }
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

private val CaptureEntrySpacing = 14.dp
