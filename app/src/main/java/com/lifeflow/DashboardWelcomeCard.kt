package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun DashboardWelcomeCard(
    state: DashboardState,
    onPrimaryAction: () -> Unit
) {
    val containerColor = when (state) {
        DashboardState.READY -> MaterialTheme.colorScheme.primaryContainer
        DashboardState.ATTENTION -> MaterialTheme.colorScheme.errorContainer
        DashboardState.HC_UNAVAILABLE,
        DashboardState.NEEDS_PERMISSIONS -> MaterialTheme.colorScheme.secondaryContainer
        DashboardState.LOADING,
        DashboardState.NO_DATA -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (state) {
        DashboardState.READY -> MaterialTheme.colorScheme.onPrimaryContainer
        DashboardState.ATTENTION -> MaterialTheme.colorScheme.onErrorContainer
        DashboardState.HC_UNAVAILABLE,
        DashboardState.NEEDS_PERMISSIONS -> MaterialTheme.colorScheme.onSecondaryContainer
        DashboardState.LOADING,
        DashboardState.NO_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dashboardWelcomeTitle(state),
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dashboardWelcomeMessage(state),
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )

            val actionLabel = dashboardPrimaryActionLabel(state)
            if (actionLabel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
