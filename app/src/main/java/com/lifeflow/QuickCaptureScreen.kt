package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickCaptureScreen(
    lastAction: String = "Quick capture shell active",
    onPrimaryCapture: () -> Unit = {},
    onOpenCaptureLibrary: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Quick Capture") {
        GuidanceCard(
            title = "Capture something small",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "Quick Capture is a lightweight entry point for a fast thought, note, signal, or reflection. In this phase it remains a calm structure layer."
        )
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ButtonDefaultsSpacing)
            ) {
                Text(
                    text = "What this step does",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "It provides one simple capture entry point first, without turning this screen into a busy workflow. This is shell structure, not final execution.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Text(
                    text = "Primary focus",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Use one calm action to add something small before any deeper orchestration, routing, or protected behavior is connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                Button(
                    onClick = onPrimaryCapture,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start capture shell")
                }
                OutlinedButton(
                    onClick = onOpenCaptureLibrary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open capture library shell")
                }
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ButtonDefaultsSpacing)
            ) {
                Text(
                    text = "Possible capture types",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "• Quick note\n• Mood or state check\n• Reflection prompt\n• Lightweight signal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ScreenSectionSpacer()
        Card {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ButtonDefaultsSpacing)
            ) {
                Text(
                    text = "Current boundary",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "This screen is intentionally non-final. It does not assert trust-state truth, biometric authority, recovery logic, or protected execution behavior.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ScreenSectionSpacer()
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

private val ButtonDefaultsSpacing = 14.dp
