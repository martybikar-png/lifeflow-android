package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun CaptureLibraryScreen(
    onBackToQuickCapture: () -> Unit = {},
) {
    ScreenContainer(title = "Capture Library") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Library")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Capture Library") {
            Text(
                text = "Review lightweight captures.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            LifeFlowPrimaryActionButton(
                label = "Back to Quick Capture",
                onClick = onBackToQuickCapture
            )
        }
    }
}
