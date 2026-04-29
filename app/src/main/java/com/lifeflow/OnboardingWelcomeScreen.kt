package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val ONBOARDING_SPLASH_DURATION_MS = 3000L

private val WelcomeTextPrimary = Color(0xFF1E2430)
private val WelcomeTextSecondary = Color(0xFF667385)
private val WelcomeTextTertiary = Color(0xFF526072)

@Composable
fun OnboardingWelcomeScreen(
    onSplashFinished: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(ONBOARDING_SPLASH_DURATION_MS)
        onSplashFinished()
    }

    ScreenContainer(
        title = "Welcome to LifeFlow",
        centerHeader = true,
        showGoldEdge = true,
        whiteStartRatio = ScreenSplashWhiteStartRatio
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    LifeFlowSplashLogo()
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
    }
}
