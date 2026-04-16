package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DebugLineSpacing = 6.dp

@Composable
internal fun LastActionCard(lastAction: String) {
    LayoutSectionCard(title = "Last action") {
        Text(
            text = normalizeLastActionForDisplay(lastAction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun DebugCard(debugLines: List<String>) {
    LayoutSectionCard(title = "Debug") {
        if (debugLines.isEmpty()) {
            Text(
                text = "No debug lines.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            androidx.compose.foundation.layout.Column(
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
    content: @Composable ColumnScope.() -> Unit
) {
    LayoutSectionCard(
        title = title,
        leadingIconResId = leadingIconResId,
        content = content
    )
}

@Composable
private fun LayoutSectionCard(
    title: String,
    leadingIconResId: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeFlowCardShell(
        title = title,
        leadingIconResId = leadingIconResId,
        content = content
    )
}

private fun normalizeLastActionForDisplay(lastAction: String): String {
    return when {
        lastAction.isBlank() -> "No action recorded yet."
        lastAction == "—" -> "No action recorded yet."
        else -> lastAction
    }
}

