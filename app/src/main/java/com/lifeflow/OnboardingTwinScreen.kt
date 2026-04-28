package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingTwinScreen(
    onContinue: () -> Unit = {},
    onViewSummary: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Digital Twin",
        screenSubtitle = "Your adaptive rhythm.",
        headline = "Your twin takes shape",
        body = "Gentle and adaptive.",
        detailTitle = "Twin",
        detailBody = "A soft picture of your rhythm.",
        markers = listOf("Gentle", "Local", "Adaptive"),
        selectedIndex = 5,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "View summary",
        onSecondary = onViewSummary
    )
}
