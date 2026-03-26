package com.lifeflow

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lifeflow.security.BiometricAuthManager
import kotlinx.coroutines.delay

private const val INTRO_SPLASH_DURATION_MS = 3000L

@Composable
internal fun MainActivityContent(
    viewModel: MainViewModel,
    biometricAuthManager: BiometricAuthManager,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit
) {
    var uiLastAction by rememberSaveable { mutableStateOf(NO_ACTION_RECORDED) }
    var pendingSettingsRefresh by rememberSaveable { mutableStateOf(false) }
    var showIntroSplash by rememberSaveable { mutableStateOf(true) }

    fun setLastAction(message: String) {
        uiLastAction = message.ifBlank { NO_ACTION_RECORDED }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel, pendingSettingsRefresh) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onAppForegrounded()
                Lifecycle.Event.ON_STOP -> {
                    if (!pendingSettingsRefresh) {
                        viewModel.onAppBackgrounded()
                    }
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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

    LaunchedEffect(Unit) {
        delay(INTRO_SPLASH_DURATION_MS)
        showIntroSplash = false
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

    if (showIntroSplash) {
        IntroSplashScreen()
        return
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

@Composable
private fun IntroSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.lifeflow_one_icon),
            contentDescription = "LifeFlow intro splash",
            modifier = Modifier.size(132.dp)
        )
    }
}
