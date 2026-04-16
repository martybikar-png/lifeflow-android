package com.lifeflow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EnableTextPrimary = Color(0xFF1E2430)
private val EnableTextSecondary = Color(0xFF667385)
private val EnableTextTertiary = Color(0xFF526072)

private val EnableMarkerText = Color(0xFF6C788A)
private val EnableMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val EnableMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val EnableMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)

private val EnableAuraPrimary = Color(0xFF22CDF7).copy(alpha = 0.18f)
private val EnableAuraSecondary = Color(0xFF8EEBFF).copy(alpha = 0.09f)
private val EnableAuraHighlight = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@Composable
fun OnboardingPermissionsScreen(
    lastAction: String = "Onboarding permissions shell active",
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(
        title = "",
        useBackdrop = false,
        showBottomAura = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            EnableScreenBackground(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSignalPill(text = "ENABLE")

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Choose what to enable",
                    color = EnableTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(250.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gentle guidance can work better with your OK — only what you choose, refined anytime you like.",
                    color = EnableTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(240.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                EnableMarkerRow()

                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSectionPanel(title = "What may help") {
                    Text(
                        text = "Health Connect, lightweight wellbeing signals, and calmer local snapshots can make LifeFlow more useful over time.",
                        style = lifeFlowCardSummaryStyle(),
                        color = EnableTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                LifeFlowSectionPanel(title = "Always your choice") {
                    Text(
                        text = "Nothing starts without your OK. You can keep things paused, enable only part of the flow, or refine access later.",
                        style = lifeFlowCardSummaryStyle(),
                        color = EnableTextTertiary
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
            label = "Not now",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            EnablePageIndicator(
                selectedIndex = 1,
                pageCount = 10
            )
        }
    }
}

@Composable
private fun EnableMarkerRow() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            EnableMarkerSurfaceStart,
                            EnableMarkerSurfaceMid,
                            EnableMarkerSurfaceStart
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnableMarkerLabel("Clear")
            EnableMarkerSeparator()
            EnableMarkerLabel("Optional")
            EnableMarkerSeparator()
            EnableMarkerLabel("Reversible")
        }
    }
}

@Composable
private fun EnableMarkerLabel(
    text: String
) {
    Text(
        text = text,
        color = EnableMarkerText,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun EnableMarkerSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = EnableMarkerDotColor,
                shape = CircleShape
            )
    )
}

@Composable
private fun EnableScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "enableScreenAura")

    val driftX = infiniteTransition.animateFloat(
        initialValue = -0.02f,
        targetValue = 0.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 13000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enableAuraDriftX"
    )

    val driftY = infiniteTransition.animateFloat(
        initialValue = -0.015f,
        targetValue = 0.030f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 16000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enableAuraDriftY"
    )

    val radiusScale = infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.54f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 18000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enableAuraRadius"
    )

    Canvas(modifier = modifier) {
        val primaryCenter = Offset(
            x = size.width * (0.04f + driftX.value),
            y = size.height * (0.05f + driftY.value)
        )
        val primaryRadius = size.minDimension * radiusScale.value

        val secondaryCenter = Offset(
            x = size.width * (0.01f + driftX.value * 0.60f),
            y = size.height * (0.02f + driftY.value * 0.65f)
        )
        val secondaryRadius = primaryRadius * 0.56f

        drawRect(color = Color(0xFFF2F3F7))

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    EnableAuraPrimary,
                    EnableAuraSecondary,
                    Color.Transparent
                ),
                center = primaryCenter,
                radius = primaryRadius
            ),
            center = primaryCenter,
            radius = primaryRadius
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    EnableAuraHighlight,
                    Color.Transparent
                ),
                center = secondaryCenter,
                radius = secondaryRadius
            ),
            center = secondaryCenter,
            radius = secondaryRadius
        )
    }
}

@Composable
private fun EnablePageIndicator(
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
