package com.lifeflow

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lifeflow.security.BiometricAuthManager
import kotlinx.coroutines.delay

private const val INTRO_SPLASH_DURATION_MS = 3000L

@Composable
internal fun ActiveRuntimeContent(
    viewModel: ActiveRuntimeViewModelContract,
    biometricAuthManager: BiometricAuthManager,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit
) {
    var pendingSettingsRefresh by rememberSaveable { mutableStateOf(false) }
    var showIntroSplash by rememberSaveable { mutableStateOf(true) }
    val setLastAction: (String) -> Unit = { _ -> }

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

    HandleActiveRuntimePendingResumeAction(
        pending = pendingSettingsRefresh,
        onConsumePending = { pendingSettingsRefresh = false },
        onResumeAction = {
            requestActiveRuntimeRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Returned from settings; refresh requested",
                setLastAction = setLastAction
            )
        }
    )

    LaunchedEffect(Unit) {
        requestActiveRuntimeRefreshWithUiFeedback(
            viewModel = viewModel,
            requestMessage = "Startup refresh requested",
            setLastAction = setLastAction
        )
    }

    LaunchedEffect(Unit) {
        delay(INTRO_SPLASH_DURATION_MS)
        showIntroSplash = false
    }

    val screen = collectActiveRuntimeScreenSnapshot(
        viewModel = viewModel
    )

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        setLastAction(permissionResultMessage(grantedPermissions))
        viewModel.onHealthPermissionsResult(grantedPermissions)
    }

    LaunchedEffect(screen.uiState is UiState.Authenticated) {
        if (screen.uiState is UiState.Authenticated) {
            requestActiveRuntimeRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Post-auth refresh requested",
                setLastAction = setLastAction
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
        openActiveRuntimeHealthConnectSettingsWithFallback(
            appPackageName = appPackageName,
            onStartIntent = onStartIntent,
            onSettingsOpened = { pendingSettingsRefresh = true },
            onSettingsOpenFailed = { pendingSettingsRefresh = false },
            setLastAction = setLastAction
        )
    }

    val onAuthenticate: () -> Unit = {
        requestActiveRuntimeBiometricAuthentication(
            biometricAuthManager = biometricAuthManager,
            viewModel = viewModel,
            setLastAction = setLastAction
        )
    }

    val onUpgradeToCore: () -> Unit = {
        setLastAction("Upgrade to Core shell action requested. Commercial upgrade flow is not wired yet.")
    }

    if (showIntroSplash) {
        IntroSplashScreen()
        return
    }

    ActiveRuntimeScreenRouter(
        screen = screen,
        onAuthenticate = onAuthenticate,
        onGrantHealthPermissions = onGrantPermissions,
        onOpenHealthConnectSettings = onOpenHealthConnectSettings,
        onRefreshNow = {
            requestActiveRuntimeRefreshWithUiFeedback(
                viewModel = viewModel,
                requestMessage = "Manual refresh requested",
                setLastAction = setLastAction
            )
        },
        onResetVault = {
            requestActiveRuntimeVaultResetAuthentication(
                biometricAuthManager = biometricAuthManager,
                viewModel = viewModel,
                setLastAction = setLastAction
            )
        },
        onUpgradeToCore = onUpgradeToCore
    )
}

@Composable
private fun IntroSplashScreen() {
    ScreenContainer(title = "Welcome to LifeFlow", centerHeader = true, showGoldEdge = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(586.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adaptive care",
                    color = androidx.compose.ui.graphics.Color(0xFF526072),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(220.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WelcomeLivingCore(
                            modifier = Modifier
                                .size(196.dp)
                                .offset(y = (-18).dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.lifeflow_core_in_hands_softer),
                            contentDescription = "LifeFlow living core in caring hands",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "A calmer way to live",
                    color = androidx.compose.ui.graphics.Color(0xFF1E2430),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(246.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Gentle guidance around you.",
                    color = androidx.compose.ui.graphics.Color(0xFF667385),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(226.dp)
                )
            }
        }
    }
}
