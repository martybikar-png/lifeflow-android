package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingTrustScreen(
    onContinue: () -> Unit = {},
    onHowPrivacyWorks: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Trust",
        screenSubtitle = "Security and access.",
        headline = "Quiet trust",
        body = "Private by default.",
        detailTitle = "Trust",
        detailBody = "Your notes stay yours.",
        markers = listOf("Private", "Device-bound", "Your control"),
        selectedIndex = 3,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "How privacy works",
        onSecondary = onHowPrivacyWorks
    )
}
