package com.lifeflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityAccessSession

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LifeFlowApplication
        val startupReady = app.ensureStartupInitialized()

        if (startupReady) {
            viewModel = ViewModelProvider(
                this,
                app.mainViewModelFactory
            )[MainViewModel::class.java]

            biometricAuthManager = BiometricAuthManager(this)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!startupReady) {
                        StartupFailureContent(app = app)
                    } else {
                        MainActivityContent()
                    }
                }
            }
        }
    }

    @Composable
    private fun StartupFailureContent(app: LifeFlowApplication) {
        var startupFailureMessage by remember {
            mutableStateOf(
                app.startupFailureMessage ?: "Application startup failed for an unknown reason."
            )
        }
        var lastAction by remember { mutableStateOf("Startup initialization failed") }
        var pendingSettingsRetry by remember { mutableStateOf(false) }

        val lifecycleOwner = LocalLifecycleOwner.current

        fun retryStartup(requestMessage: String) {
            lastAction = requestMessage

            val initialized = runCatching {
                app.ensureStartupInitialized()
            }.getOrElse {
                false
            }

            if (initialized) {
                lastAction = "$requestMessage; startup recovered, recreating activity"
                recreate()
            } else {
                startupFailureMessage =
                    app.startupFailureMessage ?: "Application startup failed for an unknown reason."
                lastAction = "$requestMessage; startup still failing"
            }
        }

        fun openAppSettings() {
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }

            runCatching {
                startActivity(appSettingsIntent)
            }.onSuccess {
                pendingSettingsRetry = true
                lastAction = "Opened App settings"
            }.onFailure {
                pendingSettingsRetry = false
                lastAction = "Unable to open App settings: ${it::class.java.simpleName}"
            }
        }

        DisposableEffect(lifecycleOwner, pendingSettingsRetry) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && pendingSettingsRetry) {
                    pendingSettingsRetry = false
                    retryStartup("Returned from settings; startup retry requested")
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        StartupFailureScreen(
            message = startupFailureMessage,
            lastAction = lastAction,
            onRetryStartup = {
                retryStartup("Manual startup retry requested")
            },
            onOpenAppSettings = {
                openAppSettings()
            }
        )
    }

    @Composable
    private fun MainActivityContent() {
        var lastAction by remember { mutableStateOf("—") }
        var pendingSettingsRefresh by remember { mutableStateOf(false) }

        val lifecycleOwner = LocalLifecycleOwner.current

        fun triggerFullRefresh(requestMessage: String) {
            runCatching {
                viewModel.refreshMetricsAndTwinNow()
            }.onSuccess {
                lastAction = requestMessage
            }.onFailure {
                lastAction =
                    "Refresh trigger failed: ${it::class.java.simpleName}: ${it.message}"
            }
        }

        DisposableEffect(lifecycleOwner, pendingSettingsRefresh) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && pendingSettingsRefresh) {
                    pendingSettingsRefresh = false
                    triggerFullRefresh("Returned from settings; refresh requested")
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(Unit) {
            triggerFullRefresh("Startup refresh requested")
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
            lastAction = "HC callback: ${grantedPermissions.size} granted; refresh requested"
            viewModel.onHealthPermissionsResult(grantedPermissions)
        }

        LaunchedEffect(uiState is UiState.Authenticated) {
            if (uiState is UiState.Authenticated) {
                triggerFullRefresh("Post-auth refresh requested")
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
                lastAction = "Permission request launched"
                runCatching { permissionsLauncher.launch(required) }
                    .onFailure {
                        lastAction =
                            "Launcher FAILED: ${it::class.java.simpleName}: ${it.message}"
                    }
            }
        }

        val onOpenHealthConnectSettings: () -> Unit = {
            val hcSettingsIntent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }

            runCatching {
                startActivity(hcSettingsIntent)
            }.onSuccess {
                pendingSettingsRefresh = true
                lastAction = "Opened Health Connect settings"
            }.onFailure { primaryError ->
                runCatching {
                    startActivity(appSettingsIntent)
                }.onSuccess {
                    pendingSettingsRefresh = true
                    lastAction =
                        "HC settings unavailable (${primaryError::class.java.simpleName}). Opened App settings instead."
                }.onFailure { fallbackError ->
                    pendingSettingsRefresh = false
                    lastAction =
                        "Unable to open settings: ${primaryError::class.java.simpleName} / ${fallbackError::class.java.simpleName}"
                }
            }
        }

        val onAuthenticate: () -> Unit = {
            lastAction = "Biometric authentication requested"
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
                val hasActiveSession = SecurityAccessSession.isAuthorized()
                LoadingScreen(
                    isAuthenticating = hasActiveSession,
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
                        triggerFullRefresh("Manual refresh requested")
                    },
                    onGrantHealthPermissions = onGrantPermissions,
                    onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                    onReAuthenticate = onAuthenticate,
                    debugLines = debugLines
                )
            }

            is UiState.Error -> {
                val message = uiState.message
                val resetRequired = message.contains(
                    "Reset vault is required",
                    ignoreCase = true
                ) || message.contains(
                    "Security compromised",
                    ignoreCase = true
                )
                val allowAuthenticate = !resetRequired

                ErrorScreen(
                    message = message,
                    healthState = hcState,
                    requiredCount = required.size,
                    grantedCount = granted.size,
                    stepsGranted = stepsGranted,
                    hrGranted = hrGranted,
                    lastAction = lastAction,
                    onAuthenticate = onAuthenticate,
                    onGrantHealthPermissions = onGrantPermissions,
                    onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                    onResetVault = {
                        lastAction = "Vault reset requested"
                        viewModel.resetVault()
                    },
                    debugLines = debugLines,
                    showAuthenticateAction = allowAuthenticate,
                    showResetVaultAction = resetRequired
                )
            }
        }
    }
}

@Composable
private fun StartupFailureScreen(
    message: String,
    lastAction: String,
    onRetryStartup: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "LifeFlow Startup Error",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Application startup failed",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recovery actions",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRetryStartup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retry startup")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open App settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Last action",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lastAction,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}