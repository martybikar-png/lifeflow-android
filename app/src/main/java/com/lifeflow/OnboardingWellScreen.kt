package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingWellScreen(
    onContinue: () -> Unit = {},
    onAnotherTime: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Wellbeing",
        screenSubtitle = "A gentle state check.",
        headline = "A softer snapshot",
        body = "Gentle and balanced.",
        detailTitle = "Wellbeing",
        detailBody = "A kind read of your current state.",
        markers = listOf("Gentle", "Balanced", "Supportive"),
        selectedIndex = 4,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Another time",
        onSecondary = onAnotherTime
    )
}
