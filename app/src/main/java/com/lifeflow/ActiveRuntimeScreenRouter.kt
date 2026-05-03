package com.lifeflow

import androidx.compose.runtime.Composable
import com.lifeflow.navigation.ProtectedRuntimeNavHost

@Composable
internal fun ActiveRuntimeScreenRouter(
    screen: ActiveRuntimeScreenSnapshot,
    onSaveQuickCapture: (String) -> Unit = {},
    onLoadQuickCaptureLibrary: () -> Unit = {},
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onRefreshNow: () -> Unit,
    onResetVault: () -> Unit,
    onUpgradeToCore: () -> Unit = {}
) {
    when (screen.uiState) {
        UiState.Loading -> {
            ProtectedLoginScreen(
                isAuthenticating = false,
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )
        }

        UiState.Authenticated -> {
            ProtectedRuntimeNavHost(
                screen = screen,
                onSaveQuickCapture = onSaveQuickCapture,
                onLoadQuickCaptureLibrary = onLoadQuickCaptureLibrary,
                onAuthenticate = onAuthenticate,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onRefreshNow = onRefreshNow,
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
