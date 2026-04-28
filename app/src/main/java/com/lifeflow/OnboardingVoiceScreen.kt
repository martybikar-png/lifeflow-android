package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingVoiceScreen(
    onContinue: () -> Unit = {},
    onStayQuiet: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Voice Link",
        screenSubtitle = "Reach someone gently.",
        headline = "Reach someone gently",
        body = "Human, calm, direct.",
        detailTitle = "Voice",
        detailBody = "A quiet path to contact.",
        markers = listOf("Human", "Calm", "Direct"),
        selectedIndex = 9,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Stay quiet",
        onSecondary = onStayQuiet
    )
}
