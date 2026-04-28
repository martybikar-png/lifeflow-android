package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CaptureEntryScreen(
    onBackToQuickCapture: () -> Unit = {},
) {
    ScreenContainer(
        title = "Capture Entry",
        showBackButton = true,
        onBack = onBackToQuickCapture,
        showGoldEdge = true
    ) {
        LifeFlowSectionPanel(title = "New capture") {
            Text(
                text = "Capture entry is ready.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Done",
                onClick = onBackToQuickCapture
            )
        }
    }
}
