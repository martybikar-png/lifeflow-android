package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val TrustActionColumnGap = 16.dp

@Composable
fun TrustScreen(
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Trust",
        screenSubtitle = "Security posture and access.",
        infoTitle = "Trust boundary",
        infoBody = "Device trust, recovery, and access rules stay visible before protected data opens.",
        infoMarkers = listOf("Device", "Recovery", "Fail closed"),
        actionAnchor = LifeFlowActionAnchor.LoginLowerSingleRow
    ) {
        PublicShellActionPanel {
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
