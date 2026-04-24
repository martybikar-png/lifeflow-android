package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LoadingTransitionContent(
    isAuthenticating: Boolean,
    currentStateMessage: String,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    val hasPermissionGap = grantedCount < requiredCount
    val hasAuthAction = !isAuthenticating
    val shouldShowActions = hasAuthAction || hasPermissionGap

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LifeFlowSignalPill(text = "Loading")
    }

    Spacer(modifier = Modifier.height(10.dp))

    LifeFlowSectionPanel(
        title = if (isAuthenticating) {
            "Secure access in progress"
        } else {
            "Preparing LifeFlow"
        }
    ) {
        Text(
            text = currentStateMessage,
            style = lifeFlowCardSummaryStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Health $grantedCount/$requiredCount • Steps " +
                (if (stepsGranted) "On" else "Off") +
                " • HR " +
                (if (hrGranted) "On" else "Off"),
            style = lifeFlowCardSummaryStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (shouldShowActions) {
        Spacer(modifier = Modifier.height(10.dp))

        LifeFlowSectionPanel(
            title = if (hasAuthAction) "Next step" else "Health access"
        ) {
            LoadingTransitionActions(
                hasAuthAction = hasAuthAction,
                hasPermissionGap = hasPermissionGap,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )
        }
    }
}

@Composable
private fun LoadingTransitionActions(
    hasAuthAction: Boolean,
    hasPermissionGap: Boolean,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    if (hasAuthAction) {
        LifeFlowPrimaryActionButton(
            label = "Authenticate",
            onClick = onAuthenticate
        )
        return
    }

    if (hasPermissionGap) {
        LifeFlowPrimaryActionButton(
            label = "Grant Health access",
            onClick = onGrantHealthPermissions
        )

        Spacer(modifier = Modifier.height(6.dp))

        LifeFlowPrimaryActionButton(
            label = "Open Health settings",
            onClick = onOpenHealthConnectSettings
        )
    }
}
