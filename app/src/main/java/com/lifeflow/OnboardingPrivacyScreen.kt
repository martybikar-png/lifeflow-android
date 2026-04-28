package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrivacyTextPrimary = Color(0xFF1E2430)
private val PrivacyTextSecondary = Color(0xFF667385)
private val PrivacyTextTertiary = Color(0xFF526072)

@Composable
fun OnboardingPrivacyScreen(
    onFinish: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    ScreenContainer(
        title = "",
        useBackdrop = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "Clear privacy",
                color = PrivacyTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 23.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(258.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Calm boundaries. No hidden pressure.",
                color = PrivacyTextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(252.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            LifeFlowSectionPanel(title = "Privacy") {
                Text(
                    text = "Your data stays clear, protected, and easy to understand.",
                    style = lifeFlowCardSummaryStyle(),
                    color = PrivacyTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(42.dp))

            LifeFlowPrimaryActionButton(
                label = "Continue",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrivacyPageIndicator(
                selectedIndex = 2,
                pageCount = 10
            )
        }
    }
}

@Composable
private fun PrivacyPageIndicator(
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
