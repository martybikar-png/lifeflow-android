package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun DashboardWelcomeCard(
    state: DashboardState,
    onPrimaryAction: () -> Unit
) {
    val titleColor = when (state) {
        DashboardState.READY -> MaterialTheme.colorScheme.primary
        DashboardState.ATTENTION -> MaterialTheme.colorScheme.primary
        DashboardState.HC_UNAVAILABLE,
        DashboardState.NEEDS_PERMISSIONS -> MaterialTheme.colorScheme.primary
        DashboardState.LOADING,
        DashboardState.NO_DATA -> MaterialTheme.colorScheme.onSurface
    }

    val actionLabel = dashboardPrimaryActionLabel(state)

    LifeFlowCardShell(
        title = dashboardWelcomeTitle(state),
        titleColor = titleColor,
        summary = dashboardWelcomeMessage(state)
    ) {
        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(12.dp))
            LifeFlowPrimaryActionButton(
                label = actionLabel,
                onClick = onPrimaryAction
            )
        }
    }
}
