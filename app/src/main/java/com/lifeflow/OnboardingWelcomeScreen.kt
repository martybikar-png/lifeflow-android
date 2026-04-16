package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WelcomeTextPrimary = Color(0xFF1E2430)
private val WelcomeTextSecondary = Color(0xFF667385)
private val WelcomeTextTertiary = Color(0xFF526072)

@Composable
fun OnboardingWelcomeScreen(
    lastAction: String = "Onboarding welcome shell active",
    onContinue: () -> Unit = {},
    onSkipToHome: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "", useBackdrop = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            WelcomeHeroBackground(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSignalPill(text = "LIFEFLOW")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "An intelligent life assistant with adaptive care",
                    color = WelcomeTextTertiary,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(220.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WelcomeLivingCore(
                            modifier = Modifier
                                .size(196.dp)
                                .offset(y = (-18).dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.lifeflow_core_in_hands_softer),
                            contentDescription = "LifeFlow living core in caring hands",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "A calmer way to live\nwith your health",
                    color = WelcomeTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(246.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Thoughtful guidance, gentle clarity,\nand a space designed around you.",
                    color = WelcomeTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(226.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LifeFlowPrimaryActionButton(
            label = "Continue with LifeFlow",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            WelcomePageIndicator(
                selectedIndex = 0,
                pageCount = 10
            )
        }
    }
}

@Composable
private fun WelcomeHeroBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val hazeCenter = Offset(
            x = size.width * 0.50f,
            y = size.height * 0.36f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFEAFBFF).copy(alpha = 0.34f),
                    Color(0xFFDDF8FF).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = hazeCenter,
                radius = size.minDimension * 0.40f
            ),
            radius = size.minDimension * 0.40f,
            center = hazeCenter
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.50f),
            startAngle = 198f,
            sweepAngle = 124f,
            useCenter = false,
            topLeft = Offset(-size.width * 1.02f, size.height * 0.04f),
            size = Size(size.width * 2.40f, size.height * 0.46f),
            style = Stroke(width = 3.05.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.38f),
            startAngle = 314f,
            sweepAngle = 138f,
            useCenter = false,
            topLeft = Offset(size.width * 0.34f, -size.height * 0.04f),
            size = Size(size.width * 1.76f, size.height * 0.60f),
            style = Stroke(width = 2.42.dp.toPx())
        )

        drawArc(
            color = Color(0xFFEAFBFF).copy(alpha = 0.21f),
            startAngle = 22f,
            sweepAngle = 88f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.46f, size.height * 0.82f),
            size = Size(size.width * 1.18f, size.height * 0.14f),
            style = Stroke(width = 2.00.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.34f),
            startAngle = 154f,
            sweepAngle = 154f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.58f, size.height * 0.26f),
            size = Size(size.width * 1.68f, size.height * 0.34f),
            style = Stroke(width = 1.75.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.28f),
            startAngle = 338f,
            sweepAngle = 146f,
            useCenter = false,
            topLeft = Offset(size.width * 0.08f, size.height * 0.18f),
            size = Size(size.width * 1.30f, size.height * 0.40f),
            style = Stroke(width = 1.55.dp.toPx())
        )

        drawArc(
            color = Color(0xFFEAFBFF).copy(alpha = 0.18f),
            startAngle = 168f,
            sweepAngle = 122f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.22f, size.height * 0.32f),
            size = Size(size.width * 1.04f, size.height * 0.20f),
            style = Stroke(width = 1.25.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.35f),
            startAngle = 18f,
            sweepAngle = 124f,
            useCenter = false,
            topLeft = Offset(-size.width * 1.00f, size.height * 0.66f),
            size = Size(size.width * 2.34f, size.height * 0.34f),
            style = Stroke(width = 2.15.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.27f),
            startAngle = 34f,
            sweepAngle = 126f,
            useCenter = false,
            topLeft = Offset(size.width * 0.26f, size.height * 0.62f),
            size = Size(size.width * 1.68f, size.height * 0.40f),
            style = Stroke(width = 1.72.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.24f),
            startAngle = 52f,
            sweepAngle = 118f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.54f, size.height * 0.58f),
            size = Size(size.width * 1.56f, size.height * 0.26f),
            style = Stroke(width = 1.45.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.20f),
            startAngle = 8f,
            sweepAngle = 102f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.18f, size.height * 0.74f),
            size = Size(size.width * 1.02f, size.height * 0.16f),
            style = Stroke(width = 1.18.dp.toPx())
        )
    }
}

@Composable
private fun WelcomePageIndicator(
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

