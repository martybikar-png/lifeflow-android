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

private val TrustTextPrimary = Color(0xFF1E2430)
private val TrustTextSecondary = Color(0xFF667385)
private val TrustTextTertiary = Color(0xFF526072)

private val TrustMarkerText = Color(0xFF6C788A)
private val TrustMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val TrustMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val TrustMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)
@Composable
internal fun OnboardingTrustScreen(
    onContinue: () -> Unit = {},
    onHowPrivacyWorks: () -> Unit = {},
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

                LifeFlowSignalPill(text = "TRUST")

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Quiet trust",
                    color = TrustTextPrimary,
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
                    text = "Private by default.",
                    color = TrustTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(238.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TrustMarkerRow()

                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSectionPanel(title = "Trust") {
                    Text(
                        text = "Your notes stay yours.",
                        style = lifeFlowCardSummaryStyle(),
                        color = TrustTextTertiary
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
            label = "How privacy works",
            onClick = onHowPrivacyWorks,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TrustPageIndicator(
                selectedIndex = 3,
                pageCount = 10
            )
        }
    }
}

@Composable
private fun TrustMarkerRow() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            TrustMarkerSurfaceStart,
                            TrustMarkerSurfaceMid,
                            TrustMarkerSurfaceStart
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustMarkerLabel("Private")
            TrustMarkerSeparator()
            TrustMarkerLabel("Device-bound")
            TrustMarkerSeparator()
            TrustMarkerLabel("Your control")
        }
    }
}

@Composable
private fun TrustMarkerLabel(
    text: String
) {
    Text(
        text = text,
        color = TrustMarkerText,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun TrustMarkerSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = TrustMarkerDotColor,
                shape = CircleShape
            )
    )
}

@Composable
private fun TrustPageIndicator(
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
