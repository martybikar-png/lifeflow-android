package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val HomePrimaryPage = 0
private const val HomeSecondaryPage = 1
private val HomeActionRowLargeGap = 58.dp
@Composable
fun HomeScreen(
    onOpenDashboard: () -> Unit = {},
    onOpenQuickCapture: () -> Unit = {},
    onOpenWellbeing: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
) {
    var page by rememberSaveable { mutableIntStateOf(HomePrimaryPage) }

    PublicShellInfoActionScreen(
        screenTitle = "Home",
        screenSubtitle = "One calm next step.",
        infoTitle = "Begin with one small signal.",
        infoBody = "Capture, wellbeing, and journal stay close without exposing protected data.",
        infoMarkers = listOf("Simple", "Calm", "Ready"),
        showGoldEdge = true,
        actionAnchor = LifeFlowActionAnchor.LoginLowerTwoRows
    ) {
        PublicShellActionPanel {
            if (page == HomePrimaryPage) {
                HomePrimaryActions(
                    onOpenDashboard = onOpenDashboard,
                    onOpenQuickCapture = onOpenQuickCapture,
                    onOpenWellbeing = onOpenWellbeing,
                    onNext = { page = HomeSecondaryPage }
                )
            } else {
                HomeSecondaryActions(
                    onOpenJournal = onOpenJournal,
                    onOpenSettings = onOpenSettings,
                    onOpenTrust = onOpenTrust,
                    onBack = { page = HomePrimaryPage }
                )
            }
        }
    }
}

@Composable
private fun HomePrimaryActions(
    onOpenDashboard: () -> Unit,
    onOpenQuickCapture: () -> Unit,
    onOpenWellbeing: () -> Unit,
    onNext: () -> Unit
) {
    HomeActionRow {
        LifeFlowHomePrimaryActionButton(
            label = "Dashboard",
            onClick = onOpenDashboard,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Quick Capture",
            onClick = onOpenQuickCapture,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(HomeActionRowLargeGap))

    HomeActionRow {
        LifeFlowHomeSecondaryActionButton(
            label = "Wellbeing",
            onClick = onOpenWellbeing,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Next →",
            onClick = onNext,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeSecondaryActions(
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTrust: () -> Unit,
    onBack: () -> Unit
) {
    HomeActionRow {
        LifeFlowHomePrimaryActionButton(
            label = "Journal",
            onClick = onOpenJournal,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Settings",
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(HomeActionRowLargeGap))

    HomeActionRow {
        LifeFlowHomeSecondaryActionButton(
            label = "Trust",
            onClick = onOpenTrust,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "← Back",
            onClick = onBack,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeActionRow(
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
