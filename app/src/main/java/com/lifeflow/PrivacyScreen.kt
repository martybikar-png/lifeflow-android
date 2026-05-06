package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val PrivacyActionPanelVerticalOffset = (-40).dp
private val PrivacyActionColumnGap = 16.dp

@Composable
fun PrivacyScreen(
    onOpenTrust: () -> Unit = {},
    onBackToSettings: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Privacy",
        screenSubtitle = "Data boundaries stay clear.",
        infoTitle = "Privacy",
        infoBody = "Data boundaries stay clear.",
        infoMarkers = listOf("Local", "Clear", "Yours"),
    ) {
        PublicShellActionPanel(
            modifier = Modifier.offset(y = PrivacyActionPanelVerticalOffset)
        ) {
            PrivacyActionRow {
                LifeFlowHomePrimaryActionButton(
                    label = "Trust",
                    onClick = onOpenTrust,
                    modifier = Modifier.weight(1f)
                )
                LifeFlowHomeSecondaryActionButton(
                    label = "Back",
                    onClick = onBackToSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PrivacyActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PrivacyActionColumnGap),
        content = content
    )
}
