package com.lifeflow

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.health.connect.client.PermissionController
import com.lifeflow.security.BiometricAuthManager

@Composable
internal fun MainActivityContent(
    viewModel: MainViewModel,
    biometricAuthManager: BiometricAuthManager,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit
) {
    var uiLastAction by rememberSaveable { mutableStateOf(NO_ACTION_RECORDED) }
    var pendingSettingsRefresh by rememberSaveable { mutableStateOf(false) }

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

    val onUpgradeToCore: () -> Unit = {
        setLastAction("Upgrade to Core shell action requested. Commercial upgrade flow is not wired yet.")
    }

    MainActivityScreenRouter(
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
        },
        onUpgradeToCore = onUpgradeToCore
    )
}
