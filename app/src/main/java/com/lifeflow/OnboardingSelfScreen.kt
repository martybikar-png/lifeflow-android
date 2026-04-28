package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun OnboardingSelfScreen(
    onContinue: () -> Unit = {},
    onAnotherTime: () -> Unit = {},
) {
    OnboardingStepScreen(
        screenTitle = "Self Space",
        screenSubtitle = "A quieter personal area.",
        headline = "Your quiet center",
        body = "Personal and calm.",
        detailTitle = "Self",
        detailBody = "A place that feels like yours.",
        markers = listOf("Quiet", "Personal", "Yours"),
        selectedIndex = 7,
        primaryLabel = "Continue",
        onPrimary = onContinue,
        secondaryLabel = "Another time",
        onSecondary = onAnotherTime
    )
}
