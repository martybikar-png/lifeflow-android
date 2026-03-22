package com.lifeflow

import androidx.compose.runtime.Composable
import com.lifeflow.navigation.LifeFlowNavHost

@Composable
internal fun MainActivityScreenRouter(
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
            LifeFlowNavHost()
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
