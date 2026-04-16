package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SharedCardShape = RoundedCornerShape(30.dp)
private val SharedCardPadding = 22.dp
private val SharedCardContentSpacing = 16.dp
private val SharedCardHeaderSpacing = 8.dp
private val SharedCardIconSize = 18.dp
private val SharedCardChromePadding = 2.dp

private val SectionPanelShape = RoundedCornerShape(24.dp)
private val SectionPanelPadding = 18.dp
private val SectionPanelSpacing = 10.dp

private val NeuPanelOuterPadding = 12.dp

@Composable
private fun LifeFlowNeuPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    contentPadding: Dp,
    contentSpacing: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(NeuPanelOuterPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .lifeFlowRaisedPanelChrome(shape)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content
        )
    }
}

@Composable
internal fun lifeFlowCardTitleStyle(): TextStyle {
    return MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
}

@Composable
internal fun lifeFlowCardSummaryStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
}

@Composable
internal fun lifeFlowCardRowLabelStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
}

@Composable
internal fun lifeFlowCardRowValueStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
}

@Composable
internal fun LifeFlowCardShell(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIconResId: Int? = null,
    summary: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeFlowNeuPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(SharedCardChromePadding),
        shape = SharedCardShape,
        contentPadding = SharedCardPadding,
        contentSpacing = SharedCardContentSpacing
    ) {
        if (leadingIconResId != null || title.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SharedCardHeaderSpacing)
            ) {
                if (leadingIconResId != null) {
                    Image(
                        painter = painterResource(id = leadingIconResId),
                        contentDescription = null,
                        modifier = Modifier.size(SharedCardIconSize),
                        colorFilter = ColorFilter.tint(titleColor)
                    )
                }

                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = lifeFlowCardTitleStyle(),
                        color = titleColor
                    )
                }
            }
        }

        if (!summary.isNullOrBlank()) {
            Text(
                text = summary,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        content()
    }
}

@Composable
internal fun LifeFlowSectionPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeFlowNeuPanel(
        modifier = modifier,
        shape = SectionPanelShape,
        contentPadding = SectionPanelPadding,
        contentSpacing = SectionPanelSpacing
    ) {
        Text(
            text = title,
            style = lifeFlowCardTitleStyle(),
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}
