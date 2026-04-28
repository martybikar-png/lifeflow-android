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
fun SettingsScreen(
    onOpenPrivacy: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    ScreenContainer(title = "Settings", showGoldEdge = true) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Settings")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Controls") {
            Text(
                text = "Privacy, trust, and app controls.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            LifeFlowPrimaryActionButton(
                label = "Privacy",
                onClick = onOpenPrivacy
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Back to Home",
                onClick = onBackToHome
            )
        }
    }
}
