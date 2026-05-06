package com.lifeflow

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val JournalActionPanelVerticalOffset = (-20).dp

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
        "Your private journal space is ready."
    } else {
        "Preview the journal without opening entries."
    }

    PublicShellInfoActionScreen(
        screenTitle = "Journal",
        screenSubtitle = if (isProtectedSurface) "Private space." else "Quiet preview.",
        infoTitle = if (isProtectedSurface) "Private journal" else "Journal",
        infoBody = buildJournalBody(body, statusMessage),
        infoMarkers = markers,
        showGoldEdge = true
    ) {
        PublicShellActionPanel(
            modifier = Modifier.offset(y = JournalActionPanelVerticalOffset)
        ) {
            LifeFlowHomeSecondaryActionButton(
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
        body
    }
}
