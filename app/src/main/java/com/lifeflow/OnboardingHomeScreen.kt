package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingHomeScreen(
    onContinue: () -> Unit = {},
    onPause: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Home",
        screenSubtitle = "One calm next step.",
        headline = "One calm next step",
        body = "Home stays clear and quiet.",
        detailTitle = "Home",
        detailBody = "One main move. The rest stays soft.",
        markers = listOf("Focused", "Calm", "Guided"),
        selectedIndex = 6,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Pause",
        onSecondary = onPause
    )
}
