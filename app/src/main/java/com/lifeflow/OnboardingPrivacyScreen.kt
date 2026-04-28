package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
fun OnboardingPrivacyScreen(
    onFinish: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Privacy",
        screenSubtitle = "Your data boundaries.",
        headline = "Clear privacy",
        body = "Calm boundaries. No hidden pressure.",
        detailTitle = "Privacy",
        detailBody = "Your data stays clear, protected, and easy to understand.",
        markers = listOf("Clear", "Private", "Yours"),
        selectedIndex = 2,
        primaryLabel = "Continue",
        onPrimary = onFinish,
        secondaryLabel = "Back",
        onSecondary = onBack
    )
}
