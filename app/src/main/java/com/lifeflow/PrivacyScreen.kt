package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    onOpenTrust: () -> Unit = {},
    onBackToSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    ScreenContainer(
        title = "Privacy",
        showBackButton = true,
        onBack = onBackToSettings,
        showGoldEdge = true
    ) {
        LifeFlowSectionPanel(title = "Privacy") {
            Text(
                text = "Data boundaries stay clear.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )
        }
    }
}
