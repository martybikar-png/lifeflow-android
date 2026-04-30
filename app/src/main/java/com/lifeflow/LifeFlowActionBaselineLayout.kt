package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Baseline rule:
 * The shared action baseline is the lower edge of the real button/control surface.
 * It does not include shadow, glow, or the outer wrapper padding.
 */
internal val LifeFlowActionBaselineBottomPadding = 132.dp
internal val LifeFlowLoginEnterBaselineOffset = 25.dp
internal val LifeFlowActionItemSpacing = 14.dp

@Composable
internal fun LifeFlowActionFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        content = content
    )
}

@Composable
internal fun BoxScope.LifeFlowBottomAnchoredActions(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(
                bottom = LifeFlowActionBaselineBottomPadding -
                    LifeFlowButtonOuterVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifeFlowActionItemSpacing),
        content = content
    )
}
