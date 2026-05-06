package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val TrustActionPanelVerticalOffset = (-40).dp
private val TrustActionColumnGap = 16.dp

@Composable
fun TrustScreen(
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Trust",
        screenSubtitle = "Security and access.",
        infoTitle = "Trust",
        infoBody = "Security and access stay visible.",
        infoMarkers = listOf("Secure", "Visible", "Yours"),
    ) {
        PublicShellActionPanel(
            modifier = Modifier.offset(y = TrustActionPanelVerticalOffset)
        ) {
            TrustActionRow {
                LifeFlowHomePrimaryActionButton(
                    label = "Settings",
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f)
                )
                LifeFlowHomeSecondaryActionButton(
                    label = "Back",
                    onClick = onBackToHome,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TrustActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TrustActionColumnGap),
        content = content
    )
}
