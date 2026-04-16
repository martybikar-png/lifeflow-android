package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TwinTextPrimary = Color(0xFF1E2430)
private val TwinTextSecondary = Color(0xFF667385)
private val TwinTextTertiary = Color(0xFF526072)

private val TwinMarkerText = Color(0xFF6C788A)
private val TwinMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val TwinMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val TwinMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)

@Composable
internal fun OnboardingTwinScreen(
    lastAction: String = "Onboarding twin preview active",
    onContinue: () -> Unit = {},
    onViewSummary: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(
        title = "",
        useBackdrop = false,
        showBottomAura = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            TwinScreenBackground(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSignalPill(text = "TWIN")

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Your inner model starts taking shape",
                    color = TwinTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(258.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "With your OK, LifeFlow can begin forming a calm picture of what supports you, what drains you, and what helps over time.",
                    color = TwinTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(244.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TwinMarkerRow()

                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSectionPanel(title = "Learns gently") {
                    Text(
                        text = "The twin is not a score or a label. It becomes a soft internal summary shaped by your own flow.",
                        style = lifeFlowCardSummaryStyle(),
                        color = TwinTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                LifeFlowSectionPanel(title = "Built from your rhythm") {
                    Text(
                        text = "Patterns emerge from what you allow — signals, reflections, and small moments that help LifeFlow respond more calmly.",
                        style = lifeFlowCardSummaryStyle(),
                        color = TwinTextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LifeFlowPrimaryActionButton(
            label = "Continue",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LifeFlowSecondaryActionButton(
            label = "View summary",
            onClick = onViewSummary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TwinPageIndicator(
                selectedIndex = 5,
                pageCount = 10
            )
        }
    }
}

@Composable
private fun TwinMarkerRow() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            TwinMarkerSurfaceStart,
                            TwinMarkerSurfaceMid,
                            TwinMarkerSurfaceStart
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TwinMarkerLabel("Gentle")
            TwinMarkerSeparator()
            TwinMarkerLabel("Local")
            TwinMarkerSeparator()
            TwinMarkerLabel("Adaptive")
        }
    }
}

@Composable
private fun TwinMarkerLabel(
    text: String
) {
    Text(
        text = text,
        color = TwinMarkerText,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun TwinMarkerSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = TwinMarkerDotColor,
                shape = CircleShape
            )
    )
}

@Composable
private fun TwinScreenBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val topHazeCenter = Offset(
            x = size.width * 0.34f,
            y = size.height * 0.18f
        )
        val lowerHazeCenter = Offset(
            x = size.width * 0.66f,
            y = size.height * 0.62f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFEAFBFF).copy(alpha = 0.18f),
                    Color(0xFFDDF8FF).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = topHazeCenter,
                radius = size.minDimension * 0.34f
            ),
            center = topHazeCenter,
            radius = size.minDimension * 0.34f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFEAFBFF).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = lowerHazeCenter,
                radius = size.minDimension * 0.30f
            ),
            center = lowerHazeCenter,
            radius = size.minDimension * 0.30f
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.30f),
            startAngle = 196f,
            sweepAngle = 118f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.96f, size.height * 0.10f),
            size = Size(size.width * 2.22f, size.height * 0.30f),
            style = Stroke(width = 2.15.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.22f),
            startAngle = 318f,
            sweepAngle = 132f,
            useCenter = false,
            topLeft = Offset(size.width * 0.26f, size.height * 0.06f),
            size = Size(size.width * 1.64f, size.height * 0.40f),
            style = Stroke(width = 1.72.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.20f),
            startAngle = 150f,
            sweepAngle = 142f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.54f, size.height * 0.34f),
            size = Size(size.width * 1.56f, size.height * 0.22f),
            style = Stroke(width = 1.36.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.16f),
            startAngle = 338f,
            sweepAngle = 136f,
            useCenter = false,
            topLeft = Offset(size.width * 0.06f, size.height * 0.30f),
            size = Size(size.width * 1.18f, size.height * 0.26f),
            style = Stroke(width = 1.12.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.18f),
            startAngle = 18f,
            sweepAngle = 116f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.94f, size.height * 0.70f),
            size = Size(size.width * 2.12f, size.height * 0.22f),
            style = Stroke(width = 1.54.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.14f),
            startAngle = 40f,
            sweepAngle = 118f,
            useCenter = false,
            topLeft = Offset(size.width * 0.20f, size.height * 0.66f),
            size = Size(size.width * 1.46f, size.height * 0.28f),
            style = Stroke(width = 1.20.dp.toPx())
        )
    }
}

@Composable
private fun TwinPageIndicator(
    selectedIndex: Int,
    pageCount: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == selectedIndex

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFD7F6FF),
                                    Color(0xFFBFEFFF)
                                )
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = Color(0xFFC9D2DE),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}


