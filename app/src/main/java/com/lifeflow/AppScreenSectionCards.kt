package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CardContentPadding = 18.dp
private val TextSpacing = 10.dp
private val ActionHeaderSpacing = 14.dp
private val HeaderRowSpacing = 8.dp
private val SectionIconSize = 18.dp
private val DebugLineSpacing = 6.dp

@Composable
internal fun LastActionCard(lastAction: String) {
    LayoutSectionCard(
        title = "Last action",
        headerSpacing = TextSpacing
    ) {
        Text(
            text = normalizeLastActionForDisplay(lastAction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun DebugCard(debugLines: List<String>) {
    LayoutSectionCard(
        title = "Debug",
        headerSpacing = TextSpacing
    ) {
        if (debugLines.isEmpty()) {
            Text(
                text = "No debug lines.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(DebugLineSpacing)
            ) {
                debugLines.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActionCard(
    title: String,
    leadingIconResId: Int? = null,
    content: @Composable () -> Unit
) {
    LayoutSectionCard(
        title = title,
        headerSpacing = ActionHeaderSpacing,
        leadingIconResId = leadingIconResId,
        content = content
    )
}

@Composable
private fun LayoutSectionCard(
    title: String,
    headerSpacing: Dp,
    leadingIconResId: Int? = null,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CardContentPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeaderRowSpacing)
            ) {
                if (leadingIconResId != null) {
                    Image(
                        painter = painterResource(id = leadingIconResId),
                        contentDescription = null,
                        modifier = Modifier.size(SectionIconSize),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(headerSpacing))

            content()
        }
    }
}

private fun normalizeLastActionForDisplay(lastAction: String): String {
    return when {
        lastAction.isBlank() -> "No action recorded yet."
        lastAction == "—" -> "No action recorded yet."
        else -> lastAction
    }
}
