package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LoadingTransitionContent(
    isAuthenticating: Boolean,
    currentStateMessage: String,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    val hasPermissionGap = grantedCount < requiredCount
    val hasAuthAction = !isAuthenticating
    val shouldShowActions = hasAuthAction || hasPermissionGap

    PublicShellInfoActionScreen(
        screenTitle = "Starting",
        screenSubtitle = "Preparing LifeFlow.",
        infoTitle = if (isAuthenticating) "Secure check" else "Starting LifeFlow",
        infoBody = loadingTransitionMessage(
            isAuthenticating = isAuthenticating,
            currentStateMessage = currentStateMessage
        ),
        infoNote = "Health access $grantedCount/$requiredCount",
        actionTopGap = if (shouldShowActions) 188.dp else 0.dp,
        showGoldEdge = true
    ) {
        if (shouldShowActions) {
            PublicShellActionPanel {
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
}

private fun loadingTransitionMessage(
    isAuthenticating: Boolean,
    currentStateMessage: String
): String {
    return when {
        isAuthenticating -> "Checking protected access."
        currentStateMessage.isBlank() -> "Preparing your space."
        else -> currentStateMessage
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
            onClick = onAuthenticate,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    if (hasPermissionGap) {
        LifeFlowPrimaryActionButton(
            label = "Grant Health access",
            onClick = onGrantHealthPermissions,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LifeFlowSecondaryActionButton(
            label = "Open Health settings",
            onClick = onOpenHealthConnectSettings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
