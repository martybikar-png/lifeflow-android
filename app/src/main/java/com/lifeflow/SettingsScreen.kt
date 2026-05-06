package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        screenSubtitle = "Privacy and trust controls.",
        infoTitle = "Control center",
        infoBody = "Privacy and trust stay close without exposing protected data.",
        infoMarkers = listOf("Privacy", "Trust", "Local"),
        actionAnchor = LifeFlowActionAnchor.LoginLowerTwoRows
    ) {
        PublicShellActionPanel {
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
