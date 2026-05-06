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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class LifeFlowScreenGeometry(
    val screenHeight: Dp,
    val goldY: Dp,
    val contentTop: Dp
)

internal val LocalLifeFlowScreenGeometry = staticCompositionLocalOf {
    LifeFlowScreenGeometry(
        screenHeight = 0.dp,
        goldY = 0.dp,
        contentTop = 0.dp
    )
}

internal val LifeFlowActionBaselineBottomPadding = 132.dp
internal val LifeFlowLoginEnterBaselineOffset = 25.dp
internal val LifeFlowActionItemSpacing = 14.dp

internal enum class LifeFlowActionAnchor {
    LoginLowerSingleRow,
    LoginLowerTwoRows
}

internal val LifeFlowLoginLowerRowFromGoldCenter = 477.3.dp
private val LifeFlowSingleRowLabelCenterFromGroupCenter = 14.9.dp
private val LifeFlowTwoRowsLowerRowCenterFromBottom = 25.7.dp

@Composable
internal fun LifeFlowActionFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        content = content
    )
}

@Composable
internal fun BoxScope.LifeFlowGoldAnchoredActions(
    anchor: LifeFlowActionAnchor = LifeFlowActionAnchor.LoginLowerSingleRow,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val geometry = LocalLifeFlowScreenGeometry.current

    Layout(
        modifier = modifier
            .fillMaxSize()
            .align(Alignment.TopCenter),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LifeFlowActionItemSpacing),
                content = content
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        )

        val goldYPx = geometry.goldY.roundToPx()

        val targetLowerRowCenterY =
            goldYPx + LifeFlowLoginLowerRowFromGoldCenter.roundToPx()

        val groupHeight = placeable?.height ?: 0
        val lowerRowCenterInsideGroup = when (anchor) {
            LifeFlowActionAnchor.LoginLowerSingleRow ->
                groupHeight / 2 + LifeFlowSingleRowLabelCenterFromGroupCenter.roundToPx()
            LifeFlowActionAnchor.LoginLowerTwoRows ->
                groupHeight - LifeFlowTwoRowsLowerRowCenterFromBottom.roundToPx()
        }

        val x = placeable?.let { (constraints.maxWidth - it.width) / 2 } ?: 0
        val y = targetLowerRowCenterY - lowerRowCenterInsideGroup

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable?.placeRelative(x = x, y = y)
        }
    }
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
