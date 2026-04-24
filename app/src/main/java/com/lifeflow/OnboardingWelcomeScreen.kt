package com.lifeflow

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onContinue: () -> Unit = {},
    onSkipToHome: () -> Unit = {},
) {
    ScreenContainer(title = "") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSignalPill(text = "LIFEFLOW")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Adaptive care",
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
                    text = "A calmer way to live",
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
                    text = "Gentle guidance around you.",
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

        Spacer(modifier = Modifier.height(8.dp))

        LifeFlowSecondaryActionButton(
            label = "Skip to Home",
            onClick = onSkipToHome,
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
