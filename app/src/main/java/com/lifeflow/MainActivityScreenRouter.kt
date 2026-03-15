package com.lifeflow

import androidx.compose.runtime.Composable
import com.lifeflow.security.SecurityAccessSession

@Composable
internal fun MainActivityScreenRouter(
    screen: MainActivityScreenSnapshot,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onRefreshNow: () -> Unit,
    onResetVault: () -> Unit
) {
    when (val uiState = screen.uiState) {
        UiState.Loading -> {
            val hasActiveSession = SecurityAccessSession.isAuthorized()

            LoadingScreen(
                isAuthenticating = hasActiveSession,
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
            DashboardScreen(
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                digitalTwinState = screen.digitalTwinState,
                lastAction = screen.displayedLastAction,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onAuthenticate,
                debugLines = screen.debugLines
            )
        }

        is UiState.Error -> {
            val message = uiState.message
            val resetRequired = requiresVaultReset(message)
            val allowAuthenticate = !resetRequired

            ErrorScreen(
                message = message,
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                lastAction = screen.displayedLastAction,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onResetVault = onResetVault,
                debugLines = screen.debugLines,
                showAuthenticateAction = allowAuthenticate,
                showResetVaultAction = resetRequired
            )
        }
    }
}