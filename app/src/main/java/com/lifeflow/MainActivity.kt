package com.lifeflow

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
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
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        return MainViewModel(
                            repository = app.identityRepository,
                            getHealthConnectStatus = app.getHealthConnectStatusUseCase,
                            getHealthPermissions = app.getHealthPermissionsUseCase,
                            getGrantedHealthPermissions = app.getGrantedHealthPermissionsUseCase,
                            getStepsLast24h = app.getStepsLast24hUseCase,
                            getAvgHeartRateLast24h = app.getAvgHeartRateLast24hUseCase
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        )[MainViewModel::class.java]

        viewModel.bindDigitalTwin(app.digitalTwinOrchestrator)

        biometricAuthManager = BiometricAuthManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    var authRequested by remember { mutableStateOf(false) }

                    val uiState = viewModel.uiState.value
                    val hcState = viewModel.healthConnectState.value
                    val twin = viewModel.digitalTwinState.value

                    val required = viewModel.requiredHealthPermissions.value
                    val granted = viewModel.grantedHealthPermissions.value

                    val stepsReadPerm = HealthPermission.getReadPermission(StepsRecord::class)
                    val hrReadPerm = HealthPermission.getReadPermission(HeartRateRecord::class)

                    val stepsRequired = required.contains(stepsReadPerm)
                    val hrRequired = required.contains(hrReadPerm)

                    val stepsGranted = stepsRequired && granted.contains(stepsReadPerm)
                    val hrGranted = hrRequired && granted.contains(hrReadPerm)

                    // Health Connect permissions contract (works with connect-client alpha line)
                    val permissionsLauncher = rememberLauncherForActivityResult(
                        contract = PermissionController.createRequestPermissionResultContract()
                    ) { grantedPermissions: Set<String> ->
                        viewModel.onHealthPermissionsResult(grantedPermissions)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LifeFlow", style = MaterialTheme.typography.headlineMedium)

                        Spacer(Modifier.height(12.dp))
                        Text(uiState.toString(), style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "HealthConnect: ${hcState::class.simpleName ?: hcState.toString()}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("Read Steps: ${if (stepsGranted) "Granted" else "Denied"}")
                        Text("Read Heart Rate: ${if (hrGranted) "Granted" else "Denied"}")

                        Spacer(Modifier.height(12.dp))
                        Text("DigitalTwin: steps=${twin?.stepsLast24h ?: "?"}, hr=${twin?.avgHeartRateLast24h ?: "?"}")

                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Twin updated: ${twin?.lastUpdatedEpochMillis ?: "?"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (required.isNotEmpty()) {
                                    permissionsLauncher.launch(required)
                                }
                            }
                        ) { Text("Grant Health permissions") }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                authRequested = true
                                biometricAuthManager.authenticate(
                                    onSuccess = {
                                        // ✅ Fail-closed guard:
                                        // If biometric "succeeds" but session is not active,
                                        // we do NOT touch secured repository.
                                        if (!SecurityAccessSession.isAuthorized()) {
                                            viewModel.onAuthenticationError(
                                                "Biometric OK, but auth session is NOT active. " +
                                                        "This usually means you're not running the updated BiometricAuthManager (missing SecurityAccessSession.grant...)."
                                            )
                                        } else {
                                            viewModel.onAuthenticationSuccess()
                                        }
                                    },
                                    onError = { msg -> viewModel.onAuthenticationError(msg) }
                                )
                            }
                        ) { Text(if (authRequested) "Re-authenticate" else "Authenticate") }
                    }
                }
            }
        }
    }
}