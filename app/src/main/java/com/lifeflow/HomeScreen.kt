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
fun HomeScreen(
    onOpenQuickCapture: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
) {
    ScreenContainer(title = "Home", showGoldEdge = true) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Home")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Quick Capture") {
            Text(
                text = "Capture one small thing.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Open Quick Capture",
                onClick = onOpenQuickCapture
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "More") {
            LifeFlowPrimaryActionButton(
                label = "Settings",
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )
        }
    }
}
