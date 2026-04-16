package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.core.HealthConnectUiState

private val LoadingCardShape = RoundedCornerShape(28.dp)
private val LoadingInnerShape = RoundedCornerShape(22.dp)
private val LoadingPillShape = RoundedCornerShape(18.dp)

private val LoadingCardPadding = 22.dp
private val LoadingCardSpacing = 16.dp
private val LoadingCardOuterPadding = 14.dp
private val LoadingInnerPanelPadding = 16.dp

private val LoadingCardHighlightOffsetUltra = (-4).dp
private val LoadingCardHighlightOffsetFar = (-2).dp
private val LoadingCardHighlightOffsetNear = (-1).dp
private val LoadingCardDarkOffsetNear = 1.dp
private val LoadingCardDarkOffsetFar = 3.dp
private val LoadingCardDarkOffsetUltra = 5.dp

private val LoadingPillHighlightOffsetStrong = (-2).dp
private val LoadingPillHighlightOffset = (-1).dp
private val LoadingPillDarkOffset = 1.dp
private val LoadingPillDarkOffsetStrong = 3.dp

private val LoadingCardSurface = Color(0xFFF1F3F6)
private val LoadingCardSurfaceSoft = Color(0xFFF1F3F6)
private val LoadingCardInnerSurface = Color(0xFFF1F3F6)

private val LoadingLightShadowFar = Color(0xFFFFFFFF)
private val LoadingLightShadowNear = Color(0xFFFFFFFF)
private val LoadingDarkShadowFar = Color(0xFFADB7C4)
private val LoadingDarkShadowNear = Color(0xFFC8D1DC)
private val LoadingCardBorder = Color(0xFFFFFFFF)

private fun loadingCardBrush(surfaceColor: Color): Brush {
    return Brush.linearGradient(
        colors = listOf(
            surfaceColor,
            surfaceColor,
            surfaceColor
        )
    )
}

@Composable
private fun loadingEyebrowTextStyle(): TextStyle {
    return MaterialTheme.typography.labelLarge.copy(
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}

@Composable
private fun loadingHeadlineTextStyle(): TextStyle {
    return MaterialTheme.typography.headlineSmall.copy(
        fontSize = 15.sp,
        lineHeight = 19.sp
    )
}

@Composable
private fun loadingSectionTitleTextStyle(): TextStyle {
    return MaterialTheme.typography.titleLarge.copy(
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun loadingBodyTextStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun loadingPillTextStyle(): TextStyle {
    return MaterialTheme.typography.labelMedium.copy(
        fontSize = 10.sp,
        lineHeight = 12.sp
    )
}

@Composable
private fun loadingInnerTitleTextStyle(): TextStyle {
    return MaterialTheme.typography.titleSmall.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

@Composable
fun LoadingScreen(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    lastAction: String,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    debugLines: List<String>
) {
    val currentStateMessage = loadingMessage(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )
    val currentFocusMessage = loadingGuidanceMessage(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Loading")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LoadingHeroCard()
        ScreenSectionSpacer()
        LoadingStateCard(currentStateMessage = currentStateMessage)
        ScreenSectionSpacer()
        LoadingFocusCard(currentFocusMessage = currentFocusMessage)
        ScreenSectionSpacer()
        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )
        ScreenSectionSpacer()
        LoadingActionsCard(
            isAuthenticating = isAuthenticating,
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
fun FreeTierScreen(
    message: String,
    lastAction: String,
    onUpgradeToCore: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Free mode")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Free mode is active for now") {
            Text(
                text = "This screen explains the current product tier in a calm way. It should feel informative and low-pressure, not like a hard sales wall.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Current tier state") {
            Text(
                text = message,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "What Core opens") {
            Text(
                text = "Core opens the Digital Twin, biometric vault, broader module access, and deeper cross-module intelligence.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = "Upgrade to Core",
                onClick = onUpgradeToCore
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Current boundary") {
            Text(
                text = "This is a tier-state screen only. It does not define trust-state truth, biometric authority, or protected execution behavior.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
fun ErrorScreen(
    message: String,
    resetRequired: Boolean,
    lastAction: String,
    onRetry: () -> Unit,
    debugLines: List<String>
) {
    val content = resolveErrorScreenContent(
        message = message,
        resetRequired = resetRequired
    )

    ScreenContainer(title = "") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(
                text = if (resetRequired) "Reset" else "Recovery"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = content.guidanceTitle) {
            Text(
                text = content.guidanceMessage,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Current security state") {
            Text(
                text = message,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Required next step") {
            Text(
                text = content.nextStepMessage,
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowPrimaryActionButton(
                label = content.buttonLabel,
                onClick = onRetry
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LifeFlowSectionPanel(title = "Current boundary") {
            Text(
                text = "This screen reflects the active security state and the next safe action. It does not redefine trust truth, recovery authority, or protected access rules in UI.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
private fun LoadingHeroCard() {
    LoadingRaisedCardShell(
        surfaceColor = LoadingCardSurfaceSoft
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Quiet loading",
                style = loadingEyebrowTextStyle(),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Preparing the shell calmly",
                style = loadingHeadlineTextStyle()
            )
        }

        Text(
            text = "LifeFlow is checking access, session state, and health signals.",
            style = loadingBodyTextStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            LoadingPill(text = "Access check")
            LoadingPill(text = "Health state")
            LoadingPill(text = "Soft pacing")
        }
    }
}

@Composable
private fun LoadingStateCard(
    currentStateMessage: String
) {
    LoadingRaisedCardShell(
        surfaceColor = LoadingCardSurface
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Current state",
                style = loadingSectionTitleTextStyle()
            )
            Text(
                text = currentStateMessage,
                style = loadingBodyTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingFocusCard(
    currentFocusMessage: String
) {
    LoadingRaisedCardShell(
        surfaceColor = LoadingCardSurfaceSoft
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Current focus",
                style = loadingSectionTitleTextStyle()
            )
            Text(
                text = currentFocusMessage,
                style = loadingBodyTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingRaisedCardShell(
    surfaceColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(LoadingCardOuterPadding)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardHighlightOffsetUltra,
                    y = LoadingCardHighlightOffsetUltra
                )
                .shadow(
                    elevation = 8.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingLightShadowFar,
                    spotColor = LoadingLightShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardHighlightOffsetFar,
                    y = LoadingCardHighlightOffsetFar
                )
                .shadow(
                    elevation = 4.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingLightShadowFar,
                    spotColor = LoadingLightShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardHighlightOffsetNear,
                    y = LoadingCardHighlightOffsetNear
                )
                .shadow(
                    elevation = 2.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingLightShadowNear,
                    spotColor = LoadingLightShadowNear
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardDarkOffsetUltra,
                    y = LoadingCardDarkOffsetUltra
                )
                .shadow(
                    elevation = 8.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingDarkShadowFar,
                    spotColor = LoadingDarkShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardDarkOffsetFar,
                    y = LoadingCardDarkOffsetFar
                )
                .shadow(
                    elevation = 4.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingDarkShadowFar,
                    spotColor = LoadingDarkShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingCardDarkOffsetNear,
                    y = LoadingCardDarkOffsetNear
                )
                .shadow(
                    elevation = 2.dp,
                    shape = LoadingCardShape,
                    clip = false,
                    ambientColor = LoadingDarkShadowNear,
                    spotColor = LoadingDarkShadowNear
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingCardShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = loadingCardBrush(surfaceColor),
                    shape = LoadingCardShape
                )
                .border(
                    width = 1.25.dp,
                    color = LoadingCardBorder,
                    shape = LoadingCardShape
                )
                .padding(LoadingCardPadding),
            verticalArrangement = Arrangement.spacedBy(LoadingCardSpacing),
            content = content
        )
    }
}

@Composable
private fun LoadingInsetPanelShell(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = loadingCardBrush(LoadingCardInnerSurface),
                shape = LoadingInnerShape
            )
            .padding(LoadingInnerPanelPadding),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        content = content
    )
}

@Composable
private fun LoadingPill(text: String) {
    Box(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingPillHighlightOffsetStrong,
                    y = LoadingPillHighlightOffsetStrong
                )
                .shadow(
                    elevation = 4.dp,
                    shape = LoadingPillShape,
                    clip = false,
                    ambientColor = LoadingLightShadowFar,
                    spotColor = LoadingLightShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingPillShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingPillHighlightOffset,
                    y = LoadingPillHighlightOffset
                )
                .shadow(
                    elevation = 2.dp,
                    shape = LoadingPillShape,
                    clip = false,
                    ambientColor = LoadingLightShadowNear,
                    spotColor = LoadingLightShadowNear
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingPillShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingPillDarkOffsetStrong,
                    y = LoadingPillDarkOffsetStrong
                )
                .shadow(
                    elevation = 4.dp,
                    shape = LoadingPillShape,
                    clip = false,
                    ambientColor = LoadingDarkShadowFar,
                    spotColor = LoadingDarkShadowFar
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingPillShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = LoadingPillDarkOffset,
                    y = LoadingPillDarkOffset
                )
                .shadow(
                    elevation = 2.dp,
                    shape = LoadingPillShape,
                    clip = false,
                    ambientColor = LoadingDarkShadowNear,
                    spotColor = LoadingDarkShadowNear
                )
                .background(
                    color = Color.Transparent,
                    shape = LoadingPillShape
                )
        )

        Text(
            text = text,
            style = loadingPillTextStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .background(
                    brush = loadingCardBrush(LoadingCardSurfaceSoft),
                    shape = LoadingPillShape
                )
                .border(
                    width = 1.25.dp,
                    color = LoadingCardBorder,
                    shape = LoadingPillShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LoadingHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.lifeflow_one_icon),
            contentDescription = "LifeFlow One icon",
            modifier = Modifier.size(96.dp)
        )
    }
}
























