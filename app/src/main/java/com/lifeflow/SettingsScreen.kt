package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onOpenPrivacy: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    ScreenContainer(
        title = "Settings",
        showBackButton = true,
        onBack = onBackToHome,
        showGoldEdge = true
    ) {
        LifeFlowSectionPanel(title = "Controls") {
            Text(
                text = "Privacy and trust controls.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Privacy",
                onClick = onOpenPrivacy
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowSecondaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )
        }
    }
}
