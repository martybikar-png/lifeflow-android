package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun ActiveRuntimeScreenRouter(
    screen: ActiveRuntimeScreenSnapshot,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onRefreshNow: () -> Unit,
    onResetVault: () -> Unit,
    onUpgradeToCore: () -> Unit = {}
) {
    val forcePreviewErrorScreen = false

    if (forcePreviewErrorScreen) {
        ErrorScreen(
            message = "Protected access is paused until a fresh authentication step is completed.",
            resetRequired = false,
            onRetry = onAuthenticate
        )
        return
    }

    when (screen.uiState) {
        UiState.Loading -> {
            LoadingScreen(
                isAuthenticating = screen.isAuthenticating,
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )
        }

        UiState.Authenticated -> {
            AuthenticatedDashboardScreen(
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                digitalTwinState = screen.digitalTwinState,
                wellbeingAssessment = screen.wellbeingAssessment,
                boundarySnapshot = screen.boundarySnapshot,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onAuthenticate,
                onUpgradeToCore = onUpgradeToCore
            )
        }

        UiState.FreeTier -> {
            FreeTierScreen(
                message = screen.freeTierMessage.ifBlank { "Free tier active." },
                onUpgradeToCore = onUpgradeToCore
            )
        }

        is UiState.Error -> {
            val message = screen.uiState.message
            val resetRequired = requiresVaultReset(message)
            ErrorScreen(
                message = message,
                resetRequired = resetRequired,
                onRetry = if (resetRequired) onResetVault else onAuthenticate
            )
        }
    }
}
