package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenQuickCapture: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
) {
    ScreenContainer(title = "Home", showGoldEdge = true) {
        LifeFlowSectionPanel(title = "Start") {
            Text(
                text = "Begin with one small capture.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Quick Capture",
                onClick = onOpenQuickCapture
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowSecondaryActionButton(
                label = "Settings",
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowSecondaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )
        }
    }
}
