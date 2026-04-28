package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenOuterHorizontalPadding = 20.dp
private val ScreenOuterVerticalPadding = 12.dp
private val ScreenContentMaxWidth = 580.dp
private val ScreenHeaderSpacing = 14.dp
private val ScreenHeaderTextSpacing = 4.dp
private val ScreenTopBandContentSpacing = 22.dp
private const val ScreenWhiteStartRatio = 0.25f

private val ScreenSurfaceTone = Color(0xFFFFFFFF)
private val ScreenTopBlueStart = Color(0xFF22CDF7)
private val ScreenTopBlueEnd = Color(0xFF2F8FFF)

private val ScreenBoundaryBlueWideFade = Color(0xFF0A75E8).copy(alpha = 0.18f)
private val ScreenBoundaryBlueMidFade = Color(0xFF075FE0).copy(alpha = 0.24f)
private val ScreenBoundaryBlueDeepFade = Color(0xFF0646B9).copy(alpha = 0.22f)
private val ScreenBoundaryBlueSoftGlow = Color(0xFF22CDF7).copy(alpha = 0.10f)

private val ScreenGoldDividerBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE4B94D),
        Color(0xFFFFE9A8),
        Color(0xFFFFF6C8),
        Color(0xFFD49A2E),
        Color(0xFFE4B94D)
    )
)

private val ScreenWhiteForegroundShape = RoundedCornerShape(
    topStart = 44.dp,
    topEnd = 44.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

@Composable
internal fun ScreenContainer(
    title: String,
    subtitle: String = "",
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null,
    useBackdrop: Boolean = true,
    showBottomAura: Boolean = false,
    centerHeader: Boolean = false,
    showGoldEdge: Boolean = false,
    content: @Composable () -> Unit
) {
    val showHeader = (showBackButton && onBack != null) || title.isNotBlank() || subtitle.isNotBlank()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ScreenTopBlueStart,
                        ScreenTopBlueEnd
                    )
                )
            )
    ) {
        val whiteStart = maxHeight * ScreenWhiteStartRatio

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = whiteStart)
                .dropShadow(
                    shape = ScreenWhiteForegroundShape,
                    shadow = Shadow(
                        radius = 120.dp,
                        spread = 0.dp,
                        color = ScreenBoundaryBlueWideFade,
                        offset = DpOffset(x = 0.dp, y = (-42).dp)
                    )
                )
                .dropShadow(
                    shape = ScreenWhiteForegroundShape,
                    shadow = Shadow(
                        radius = 90.dp,
                        spread = 0.dp,
                        color = ScreenBoundaryBlueMidFade,
                        offset = DpOffset(x = 0.dp, y = (-30).dp)
                    )
                )
                .dropShadow(
                    shape = ScreenWhiteForegroundShape,
                    shadow = Shadow(
                        radius = 64.dp,
                        spread = 0.dp,
                        color = ScreenBoundaryBlueDeepFade,
                        offset = DpOffset(x = 0.dp, y = (-18).dp)
                    )
                )
                .dropShadow(
                    shape = ScreenWhiteForegroundShape,
                    shadow = Shadow(
                        radius = 42.dp,
                        spread = 0.dp,
                        color = ScreenBoundaryBlueSoftGlow,
                        offset = DpOffset(x = 0.dp, y = (-8).dp)
                    )
                )
                .clip(ScreenWhiteForegroundShape)
                .background(ScreenSurfaceTone)
        )

        if (showGoldEdge) {
            ScreenGoldDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = whiteStart)
            )
        }

        if (showHeader) {
            Row(
                modifier = if (centerHeader) {
                    Modifier
                        .fillMaxWidth()
                        .height(whiteStart)
                        .padding(horizontal = ScreenOuterHorizontalPadding)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = ScreenOuterHorizontalPadding,
                            vertical = ScreenOuterVerticalPadding + 10.dp
                        )
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (centerHeader) Arrangement.Center else Arrangement.spacedBy(ScreenHeaderSpacing)
            ) {
                if (showBackButton && onBack != null) {
                    LifeFlowPrimaryActionButton(
                        label = "Back",
                        onClick = onBack
                    )
                }

                Column(
                    modifier = if (centerHeader) Modifier.fillMaxWidth() else Modifier,
                    horizontalAlignment = if (centerHeader) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(ScreenHeaderTextSpacing)
                ) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 19.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            ),
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = ScreenOuterHorizontalPadding,
                    end = ScreenOuterHorizontalPadding,
                    top = whiteStart + ScreenTopBandContentSpacing,
                    bottom = ScreenOuterVerticalPadding
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ScreenContentMaxWidth)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.Top
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ScreenGoldDivider(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.height(48.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val inset = strokeWidth / 2f
        val radius = 44.dp.toPx()

        val path = Path().apply {
            moveTo(inset, radius)
            quadraticTo(inset, inset, radius, inset)
            lineTo(size.width - radius, inset)
            quadraticTo(size.width - inset, inset, size.width - inset, radius)
        }

        drawPath(
            path = path,
            brush = ScreenGoldDividerBrush,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
internal fun KeyValueLine(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = lifeFlowCardRowLabelStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = lifeFlowCardRowValueStyle(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}