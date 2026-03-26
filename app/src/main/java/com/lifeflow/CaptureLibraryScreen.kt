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
fun CaptureLibraryScreen(
    lastAction: String = "Capture library shell active",
    onBackToQuickCapture: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Capture Library") {
        GuidanceCard(
            title = "Review lightweight captures",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "Capture Library is a calm shell destination for browsing lightweight entries. In this phase it remains structure only, without protected retrieval or final storage behavior."
        )
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureLibrarySpacing)
            ) {
                Text(
                    text = "What this step does",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "It gives Quick Capture a real follow-up destination, so the flow can move from one capture entry point into a simple review shell instead of stopping at feedback only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "Library focus",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "This screen can later hold lightweight notes, short reflections, and simple signals. Right now it only establishes a dedicated place for that future flow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureLibrarySpacing)
            ) {
                Text(
                    text = "Planned shell content",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "• Recent captures\n• Lightweight notes\n• Reflection items\n• Simple signal history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CaptureLibrarySpacing)
            ) {
                Text(
                    text = "Current boundary",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "This screen does not claim storage truth, trust-state authority, biometric access, or protected retrieval. It stays inside the current UI shell phase.",
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

private val CaptureLibrarySpacing = 14.dp
