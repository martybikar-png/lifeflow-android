package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun JournalScreen(
    isProtectedSurface: Boolean = false,
    statusMessage: String = "",
    onBackToHome: () -> Unit = {}
) {
    val markers = if (isProtectedSurface) {
        listOf("Protected", "Journal", "Local")
    } else {
        listOf("Preview", "Journal", "Private")
    }

    val body = if (isProtectedSurface) {
        "Your protected journal surface is ready for local entries."
    } else {
        "Journal preview explains the surface without opening protected entries."
    }

    PublicShellInfoActionScreen(
        screenTitle = "Journal",
        screenSubtitle = if (isProtectedSurface) "Protected surface." else "Public preview.",
        infoTitle = if (isProtectedSurface) "Protected journal" else "Journal preview",
        infoBody = buildJournalBody(body, statusMessage),
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

private fun buildJournalBody(
    body: String,
    statusMessage: String
): String {
    val cleanStatus = statusMessage.trim()
    return if (cleanStatus.isBlank()) {
        body
    } else {
        "\n"
    }
}
