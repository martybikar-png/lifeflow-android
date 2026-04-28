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

private const val OnboardingPageCount = 10

private val OnboardingActionTopOffset = 379.dp

private val OnboardingTextPrimary = Color(0xFF1E2430)
private val OnboardingTextSecondary = Color(0xFF667385)
private val OnboardingTextTertiary = Color(0xFF526072)

private val OnboardingMarkerText = Color(0xFF6C788A)
private val OnboardingMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val OnboardingMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val OnboardingMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)

@Composable
internal fun OnboardingStepScreen(
    screenTitle: String,
    screenSubtitle: String,
    headline: String,
    body: String,
    detailTitle: String,
    detailBody: String,
    markers: List<String>,
    selectedIndex: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit
) {
    ScreenContainer(
        title = screenTitle,
        subtitle = screenSubtitle,
        showGoldEdge = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = headline,
                    color = OnboardingTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(258.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = body,
                    color = OnboardingTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(252.dp)
                )

                if (markers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OnboardingMarkerRow(markers = markers)
                }

                Spacer(modifier = Modifier.height(30.dp))

                OnboardingInfoText(
                    title = detailTitle,
                    body = detailBody
                )
            }

            OnboardingActionBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp)
                    .padding(top = OnboardingActionTopOffset)
            ) {
                LifeFlowPrimaryActionButton(
                    label = primaryLabel,
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LifeFlowSecondaryActionButton(
                    label = secondaryLabel,
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OnboardingPageIndicator(
                    selectedIndex = selectedIndex,
                    pageCount = OnboardingPageCount
                )
            }
        }
    }
}

@Composable
private fun OnboardingInfoText(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = lifeFlowCardTitleStyle(),
            color = OnboardingTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = body,
            style = lifeFlowCardSummaryStyle(),
            color = OnboardingTextTertiary
        )
    }
}

@Composable
private fun OnboardingActionBlock(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun OnboardingMarkerRow(
    markers: List<String>
) {
    Row(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        OnboardingMarkerSurfaceStart,
                        OnboardingMarkerSurfaceMid,
                        OnboardingMarkerSurfaceStart
                    )
                ),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        markers.forEachIndexed { index, marker ->
            OnboardingMarkerLabel(marker)
            if (index < markers.lastIndex) {
                OnboardingMarkerSeparator()
            }
        }
    }
}

@Composable
private fun OnboardingMarkerLabel(
    text: String
) {
    Text(
        text = text,
        color = OnboardingMarkerText,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun OnboardingMarkerSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = OnboardingMarkerDotColor,
                shape = CircleShape
            )
    )
}

@Composable
private fun OnboardingPageIndicator(
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
