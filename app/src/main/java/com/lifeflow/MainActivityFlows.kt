package com.lifeflow

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.health.connect.client.PermissionController
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityAccessSession

@Composable
internal fun MainActivityContent(
    viewModel: MainViewModel,
    biometricAuthManager: BiometricAuthManager,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit
) {
    var uiLastAction by remember { mutableStateOf(NO_ACTION_RECORDED) }
    var pendingSettingsRefresh by remember { mutableStateOf(false) }

    fun setLastAction(message: String) {
        uiLastAction = message.ifBlank { NO_ACTION_RECORDED }
    }

    HandlePendingResumeAction(
        pending = pendingSettingsRefresh,
        onConsumePending = { pendingSettingsRefresh = false },
        onResumeAction = {
            requestRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Returned from settings; refresh requested",
                setLastAction = ::setLastAction
            )
        }
    )

    LaunchedEffect(Unit) {
        requestRefreshWithUiFeedback(
            viewModel = viewModel,
            requestMessage = "Startup refresh requested",
            setLastAction = ::setLastAction
        )
    }

    val screen = collectMainActivityScreenSnapshot(
        viewModel = viewModel,
        uiLastAction = uiLastAction
    )

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        setLastAction(permissionResultMessage(grantedPermissions))
        viewModel.onHealthPermissionsResult(grantedPermissions)
    }

    LaunchedEffect(screen.uiState is UiState.Authenticated) {
        if (screen.uiState is UiState.Authenticated) {
            requestRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Post-auth refresh requested",
                setLastAction = ::setLastAction
            )
        }
    }

    val onGrantPermissions: () -> Unit = {
        if (screen.requiredPermissions.isEmpty()) {
            setLastAction("BLOCKED: required set is EMPTY")
        } else {
            setLastAction("Permission request launched")
            runCatching {
                permissionsLauncher.launch(screen.requiredPermissions)
            }.onFailure {
                setLastAction(
                    "Launcher FAILED: ${it::class.java.simpleName}: ${it.message}"
                )
            }
        }
    }

    val onOpenHealthConnectSettings: () -> Unit = {
        openHealthConnectSettingsWithFallback(
            appPackageName = appPackageName,
            onStartIntent = onStartIntent,
            onSettingsOpened = { pendingSettingsRefresh = true },
            onSettingsOpenFailed = { pendingSettingsRefresh = false },
            setLastAction = ::setLastAction
        )
    }

    val onAuthenticate: () -> Unit = {
        requestBiometricAuthentication(
            biometricAuthManager = biometricAuthManager,
            viewModel = viewModel,
            setLastAction = ::setLastAction
        )
    }

    MainActivityScreenContent(
        screen = screen,
        onAuthenticate = onAuthenticate,
        onGrantHealthPermissions = onGrantPermissions,
        onOpenHealthConnectSettings = onOpenHealthConnectSettings,
        onRefreshNow = {
            requestRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Manual refresh requested",
                setLastAction = ::setLastAction
            )
        },
        onResetVault = {
            setLastAction("Vault reset requested")
            viewModel.resetVault()
        }
    )
}

@Composable
private fun MainActivityScreenContent(
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