package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val SettingsActionPanelVerticalOffset = (-20).dp
private val SettingsActionRowLargeGap = 58.dp
private val SettingsActionColumnGap = 16.dp

@Composable
fun SettingsScreen(
    onOpenPrivacy: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Settings",
        screenSubtitle = "Controls for privacy and trust.",
        infoTitle = "Controls",
        infoBody = "Privacy and trust controls.",
        infoMarkers = listOf("Privacy", "Trust", "Control"),
    ) {
        PublicShellActionPanel(
            modifier = Modifier.offset(y = SettingsActionPanelVerticalOffset)
        ) {
            SettingsActionRow {
                LifeFlowHomePrimaryActionButton(
                    label = "Privacy",
                    onClick = onOpenPrivacy,
                    modifier = Modifier.weight(1f)
                )
                LifeFlowHomeSecondaryActionButton(
                    label = "Trust",
                    onClick = onOpenTrust,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(SettingsActionRowLargeGap))

            LifeFlowHomeSecondaryActionButton(
                label = "Back",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SettingsActionColumnGap),
        content = content
    )
}
