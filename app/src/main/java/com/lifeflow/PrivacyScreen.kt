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
    ScreenContainer(title = "Privacy") {
        LifeFlowSectionPanel(title = "Privacy") {
            Text(
                text = "Clear privacy and calm boundaries.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(title = "Next step") {
            LifeFlowPrimaryActionButton(
                label = "Trust",
                onClick = onOpenTrust
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Back to Settings",
                onClick = onBackToSettings
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Back to Home",
                onClick = onBackToHome
            )
        }
    }
}
