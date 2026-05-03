package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WellbeingScreen(
    isProtectedSurface: Boolean = false,
    statusMessage: String = "",
    onBackToHome: () -> Unit = {}
) {
    val markers = if (isProtectedSurface) {
        listOf("Protected", "Health", "Ready")
    } else {
        listOf("Preview", "Calm", "Private")
    }

    val body = if (isProtectedSurface) {
        "Your protected wellbeing surface is ready for deeper signals."
    } else {
        "Wellbeing preview stays calm and never shows protected health data."
    }

    PublicShellInfoActionScreen(
        screenTitle = "Wellbeing",
        screenSubtitle = if (isProtectedSurface) "Protected surface." else "Public preview.",
        infoTitle = if (isProtectedSurface) "Protected wellbeing" else "Wellbeing preview",
        infoBody = buildWellbeingBody(body, statusMessage),
        infoMarkers = markers,
        showGoldEdge = true
    ) {
        PublicShellActionPanel {
            LifeFlowSecondaryActionButton(
                label = "Home",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun buildWellbeingBody(
    body: String,
    statusMessage: String
): String {
    val cleanStatus = statusMessage.trim()
    return if (cleanStatus.isBlank()) {
        body
    } else {
        body
    }
}
