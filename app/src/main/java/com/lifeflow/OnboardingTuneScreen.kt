package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingTuneScreen(
    onContinue: () -> Unit = {},
    onLeaveAsIs: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Tune Space",
        screenSubtitle = "Adjust lightly.",
        headline = "Fine-tune lightly",
        body = "Small changes. Clear effect.",
        detailTitle = "Tune",
        detailBody = "Refine your space without noise.",
        markers = listOf("Gentle", "Flexible", "Clear"),
        selectedIndex = 8,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Leave as it is",
        onSecondary = onLeaveAsIs
    )
}
