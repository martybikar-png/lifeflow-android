package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrustScreen(
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    ScreenContainer(
        title = "Trust",
        showBackButton = true,
        onBack = onBackToHome,
        showGoldEdge = true
    ) {
        LifeFlowSectionPanel(title = "Trust") {
            Text(
                text = "Security and access stay visible.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Settings",
                onClick = onOpenSettings
            )
        }
    }
}
