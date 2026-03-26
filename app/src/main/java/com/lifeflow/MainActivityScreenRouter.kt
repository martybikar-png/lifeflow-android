package com.lifeflow

import androidx.compose.runtime.Composable

@Composable
internal fun ActiveRuntimeScreenRouter(
    screen: MainActivityScreenSnapshot,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onRefreshNow: () -> Unit,
    onResetVault: () -> Unit,
    onUpgradeToCore: () -> Unit = {}
) {
    when (val uiState = screen.uiState) {
        UiState.Loading -> {
            LoadingScreen(
                isAuthenticating = screen.isAuthenticating,
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                lastAction = screen.displayedLastAction,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                debugLines = screen.debugLines
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
                lastAction = screen.displayedLastAction,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onAuthenticate,
                debugLines = screen.debugLines
            )
        }

        is UiState.FreeTier -> {
            FreeTierScreen(
                message = uiState.message,
                lastAction = screen.displayedLastAction,
                onUpgradeToCore = onUpgradeToCore,
                debugLines = screen.debugLines
            )
        }

        is UiState.Error -> {
            val message = uiState.message
            val resetRequired = requiresVaultReset(message)
            ErrorScreen(
                message = message,
                resetRequired = resetRequired,
                lastAction = screen.displayedLastAction,
                onRetry = if (resetRequired) onResetVault else onAuthenticate,
                debugLines = screen.debugLines
            )
        }
    }
}
