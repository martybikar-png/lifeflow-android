package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenDashboard: () -> Unit = {},
    onOpenQuickCapture: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Home",
        screenSubtitle = "One calm next step.",
        infoTitle = "Begin with one small capture.",
        infoBody = "Quick Capture opens your first simple entry.\nSettings and Trust stay ready when you need control.",
        showGoldEdge = true
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Dashboard",
                onClick = onOpenDashboard,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Quick Capture",
                onClick = onOpenQuickCapture,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Settings",
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Trust",
                onClick = onOpenTrust,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
