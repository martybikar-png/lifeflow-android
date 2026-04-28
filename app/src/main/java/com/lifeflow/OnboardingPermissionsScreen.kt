package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
fun OnboardingPermissionsScreen(
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Enable",
        screenSubtitle = "Choose what LifeFlow can use.",
        headline = "Choose what to enable",
        body = "Only what you want.",
        detailTitle = "Your choice",
        detailBody = "Enable now or later. Nothing starts without your OK.",
        markers = listOf("Clear", "Optional", "Reversible"),
        selectedIndex = 1,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Not now",
        onSecondary = onBack
    )
}
