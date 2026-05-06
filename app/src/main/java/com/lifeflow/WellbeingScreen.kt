package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        "Private wellbeing signals are ready."
    } else {
        "Explore wellbeing without showing health data."
    }

    PublicShellInfoActionScreen(
        screenTitle = "Wellbeing",
        screenSubtitle = if (isProtectedSurface) "Private signals." else "Quiet preview.",
        infoTitle = if (isProtectedSurface) "Private wellbeing" else "Wellbeing",
        infoBody = buildWellbeingBody(body, statusMessage),
        infoMarkers = markers,
        showGoldEdge = true
    ) {
        PublicShellActionPanel {
            LifeFlowHomeSecondaryActionButton(
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
