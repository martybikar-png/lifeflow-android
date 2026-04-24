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
fun TrustScreen(
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    ScreenContainer(title = "Trust") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Trust")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Trust") {
            Text(
                text = "Protected access stays clear and calm.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            LifeFlowPrimaryActionButton(
                label = "Settings",
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Back to Home",
                onClick = onBackToHome
            )
        }
    }
}
