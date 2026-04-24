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

private val TwinTextPrimary = Color(0xFF1E2430)
private val TwinTextSecondary = Color(0xFF667385)
private val TwinTextTertiary = Color(0xFF526072)

private val TwinMarkerText = Color(0xFF6C788A)
private val TwinMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val TwinMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val TwinMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)
@Composable
internal fun OnboardingTwinScreen(
    onContinue: () -> Unit = {},
    onViewSummary: () -> Unit = {},
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

                LifeFlowSignalPill(text = "TWIN")

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Your twin takes shape",
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
                    text = "Gentle and adaptive.",
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

                LifeFlowSectionPanel(title = "Twin") {
                    Text(
                        text = "A soft picture of your rhythm.",
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
