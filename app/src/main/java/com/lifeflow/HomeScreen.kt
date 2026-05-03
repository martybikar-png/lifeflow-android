package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    onOpenDashboard: () -> Unit = {},
    onOpenQuickCapture: () -> Unit = {},
    onOpenWellbeing: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
) {
    PublicShellInfoActionScreen(
        screenTitle = "Home",
        screenSubtitle = "One calm next step.",
        infoTitle = "Begin with one small signal.",
        infoBody = "Capture, wellbeing, and journal stay close without exposing protected data.",
        infoMarkers = listOf("Simple", "Calm", "Ready"),
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
                label = "Wellbeing",
                onClick = onOpenWellbeing,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Journal",
                onClick = onOpenJournal,
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
