package com.lifeflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityAccessSession

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LifeFlowApplication

        viewModel = ViewModelProvider(
            this,
            app.mainViewModelFactory
        )[MainViewModel::class.java]

        biometricAuthManager = BiometricAuthManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    var lastAction by remember { mutableStateOf("—") }

                    // Startup refresh (safe)
                    LaunchedEffect(Unit) {
                        runCatching { viewModel.refreshHealthConnectStatus() }
                            .onFailure {
                                lastAction =
                                    "HC status refresh failed: ${it::class.java.simpleName}: ${it.message}"
                            }

                        runCatching { viewModel.refreshGrantedPermissions() }
                            .onFailure {
                                lastAction =
                                    "Permissions refresh failed: ${it::class.java.simpleName}: ${it.message}"
                            }

                        runCatching { viewModel.refreshMetricsAndTwinNow() }
                            .onFailure {
                                lastAction =
                                    "Startup metrics refresh failed: ${it::class.java.simpleName}: ${it.message}"
                            }

                        if (lastAction == "—") {
                            lastAction = "Startup refresh done"
                        }
                    }

                    val uiState = viewModel.uiState.value
                    val hcState = viewModel.healthConnectState.value
                    val twin = viewModel.digitalTwinState.value

                    val required = viewModel.requiredHealthPermissions.value
                    val granted = viewModel.grantedHealthPermissions.value

                    val stepsReadPerm = HealthPermission.getReadPermission(StepsRecord::class)
                    val hrReadPerm = HealthPermission.getReadPermission(HeartRateRecord::class)

                    val stepsGranted = granted.contains(stepsReadPerm)
                    val hrGranted = granted.contains(hrReadPerm)

                    val permissionsLauncher = rememberLauncherForActivityResult(
                        contract = PermissionController.createRequestPermissionResultContract()
                    ) { grantedPermissions: Set<String> ->
                        lastAction = "HC callback: ${grantedPermissions.size} granted"
                        viewModel.onHealthPermissionsResult(grantedPermissions)
                        viewModel.refreshMetricsAndTwinNow()
                    }

                    // Deterministic post-auth refresh:
                    // runs only when state changes into Authenticated
                    LaunchedEffect(uiState is UiState.Authenticated) {
                        if (uiState is UiState.Authenticated) {
                            runCatching { viewModel.refreshMetricsAndTwinNow() }
                                .onFailure {
                                    lastAction =
                                        "Post-auth refresh failed: ${it::class.java.simpleName}: ${it.message}"
                                }

                            if (!lastAction.startsWith("Post-auth refresh failed")) {
                                lastAction = "Post-auth refresh done"
                            }
                        }
                    }

                    val debugLines = listOf(
                        "UI: ${
                            when (uiState) {
                                UiState.Loading -> "Loading"
                                UiState.Authenticated -> "Authenticated"
                                is UiState.Error -> "Error"
                            }
                        }",
                        "Required perms: ${required.size}",
                        "Granted perms: ${granted.size}"
                    )

                    val onGrantPermissions: () -> Unit = {
                        if (required.isEmpty()) {
                            lastAction = "BLOCKED: required set is EMPTY"
                        } else {
                            runCatching { permissionsLauncher.launch(required) }
                                .onFailure {
                                    lastAction =
                                        "Launcher FAILED: ${it::class.java.simpleName}: ${it.message}"
                                }
                        }
                    }

                    val onOpenHealthConnectSettings: () -> Unit = {
                        val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                        runCatching {
                            startActivity(intent)
                            lastAction = "Opened Health Connect settings"
                        }.onFailure { t ->
                            val appSettings =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            runCatching { startActivity(appSettings) }
                            lastAction =
                                "HC settings failed (${t::class.java.simpleName}). Opened App settings instead."
                        }
                    }

                    val onAuthenticate: () -> Unit = {
                        biometricAuthManager.authenticate(
                            onSuccess = {
                                viewModel.onAuthenticationSuccess()
                            },
                            onError = { msg ->
                                viewModel.onAuthenticationError(msg)
                            }
                        )
                    }

                    when (uiState) {
                        UiState.Loading -> {
                            val isAuthenticating = SecurityAccessSession.isAuthorized()
                            LoadingScreen(
                                isAuthenticating = isAuthenticating,
                                healthState = hcState,
                                requiredCount = required.size,
                                grantedCount = granted.size,
                                stepsGranted = stepsGranted,
                                hrGranted = hrGranted,
                                lastAction = lastAction,
                                onAuthenticate = onAuthenticate,
                                onGrantHealthPermissions = onGrantPermissions,
                                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                                debugLines = debugLines
                            )
                        }

                        UiState.Authenticated -> {
                            DashboardScreen(
                                healthState = hcState,
                                requiredCount = required.size,
                                grantedCount = granted.size,
                                stepsGranted = stepsGranted,
                                hrGranted = hrGranted,
                                digitalTwinState = twin,
                                lastAction = lastAction,
                                onRefreshNow = {
                                    viewModel.refreshMetricsAndTwinNow()
                                    lastAction = "Manual refresh requested"
                                },
                                onGrantHealthPermissions = onGrantPermissions,
                                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                                onReAuthenticate = onAuthenticate,
                                debugLines = debugLines
                            )
                        }

                        is UiState.Error -> {
                            ErrorScreen(
                                message = uiState.message,
                                healthState = hcState,
                                requiredCount = required.size,
                                grantedCount = granted.size,
                                stepsGranted = stepsGranted,
                                hrGranted = hrGranted,
                                lastAction = lastAction,
                                onAuthenticate = onAuthenticate,
                                onGrantHealthPermissions = onGrantPermissions,
                                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                                onResetVault = { viewModel.resetVault() },
                                debugLines = debugLines
                            )
                        }
                    }
                }
            }
        }
    }
}