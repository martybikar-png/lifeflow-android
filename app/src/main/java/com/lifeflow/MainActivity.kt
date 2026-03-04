package com.lifeflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityAdversarialSuite
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var viewModel: MainViewModel

    // ✅ Suite is a checkpoint tool; keep OFF by default
    private val runAdversarialSuiteOnStart: Boolean = false

    // UI states owned by Activity (simple & explicit)
    private val stepsLast24h = mutableStateOf<Long?>(null)
    private val avgHrLast24h = mutableStateOf<Long?>(null)
    private val healthMessage = mutableStateOf<String?>(null)

    private val healthPermissionsLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        val app = application as LifeFlowApplication
        val required = app.getHealthPermissionsUseCase()
        val allGranted = grantedPermissions.containsAll(required)

        if (allGranted) {
            healthMessage.value = "Health Connect permissions granted."
            // Best-effort reads after permissions
            readStepsLast24h()
            readAvgHeartRateLast24h()
        } else {
            healthMessage.value = "Health permissions not granted."
        }

        viewModel.refreshHealthConnectStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LifeFlowApplication

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                repository = app.identityRepository,
                getHealthConnectStatus = app.getHealthConnectStatusUseCase
            )
        )[MainViewModel::class.java]

        biometricAuthManager = BiometricAuthManager(this)

        setContent {
            when (val state = viewModel.uiState.value) {
                UiState.Loading -> LoadingScreen()

                UiState.Authenticated -> DashboardScreen(
                    healthState = viewModel.healthConnectState.value,
                    stepsLast24h = stepsLast24h.value,
                    avgHrLast24h = avgHrLast24h.value,
                    message = healthMessage.value,
                    onConnectHealth = { connectHealth() },
                    onReadSteps = { readStepsLast24h() },
                    onReadHeartRate = { readAvgHeartRateLast24h() }
                )

                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onResetVault = { resetVaultAndReauth(app) }
                )
            }
        }

        startAuthentication(app)
    }

    private fun startAuthentication(app: LifeFlowApplication) {
        biometricAuthManager.authenticate(
            onSuccess = {
                // 1) Grant session first (your security gate)
                SecurityAccessSession.grant(durationMs = 30_000)

                // 2) Refresh Health Connect availability right after auth
                viewModel.refreshHealthConnectStatus()

                if (runAdversarialSuiteOnStart) {
                    lifecycleScope.launch {
                        SecurityAdversarialSuite.runAll(
                            repository = app.encryptedIdentityRepository,
                            blobStore = app.identityBlobStore,
                            keyManager = app.keyManager,
                            vault = app.androidVault
                        )

                        if (SecurityRuleEngine.getTrustState() != TrustState.COMPROMISED) {
                            viewModel.onAuthenticationSuccess()
                        } else {
                            SecurityAccessSession.clear()
                            viewModel.onAuthenticationError("Security compromised (adversarial suite).")
                        }
                    }
                } else {
                    viewModel.onAuthenticationSuccess()
                }
            },
            onError = { error ->
                SecurityAccessSession.clear()
                viewModel.onAuthenticationError(error)
            }
        )
    }

    private fun connectHealth() {
        val app = application as LifeFlowApplication
        val status = app.getHealthConnectStatusUseCase()

        when (status) {
            WellbeingRepository.SdkStatus.NotSupported -> {
                healthMessage.value = "Health Connect not supported on this device."
                return
            }

            WellbeingRepository.SdkStatus.NotInstalled -> {
                healthMessage.value = "Health Connect not installed."
                return
            }

            WellbeingRepository.SdkStatus.UpdateRequired -> {
                healthMessage.value = "Health Connect update required."
                return
            }

            WellbeingRepository.SdkStatus.Available -> Unit
        }

        lifecycleScope.launch {
            try {
                val granted = app.getGrantedHealthPermissionsUseCase()
                val required = app.getHealthPermissionsUseCase()

                if (granted.containsAll(required)) {
                    healthMessage.value = "Health Connect already connected."
                    return@launch
                }

                healthMessage.value = "Requesting Health permissions…"
                healthPermissionsLauncher.launch(required)

            } catch (t: Throwable) {
                healthMessage.value = "Health Connect error: ${t.message ?: "unknown error"}"
            }
        }
    }

    private fun ensureSessionAndAvailabilityOrExplain(): Boolean {
        if (!SecurityAccessSession.isAuthorized()) {
            healthMessage.value = "Session expired. Please re-authenticate."
            return false
        }

        val app = application as LifeFlowApplication
        val status = app.getHealthConnectStatusUseCase()
        if (status != WellbeingRepository.SdkStatus.Available) {
            healthMessage.value = "Health Connect not available: $status"
            return false
        }

        return true
    }

    private fun readStepsLast24h() {
        if (!ensureSessionAndAvailabilityOrExplain()) return

        lifecycleScope.launch {
            try {
                val app = application as LifeFlowApplication
                val granted = app.getGrantedHealthPermissionsUseCase()
                val required = app.getHealthPermissionsUseCase()

                if (!granted.containsAll(required)) {
                    healthMessage.value = "Missing permissions. Tap Connect Health first."
                    return@launch
                }

                val steps = app.getStepsLast24hUseCase()
                stepsLast24h.value = steps
                healthMessage.value = "Steps loaded."

            } catch (t: Throwable) {
                healthMessage.value = "Read steps failed: ${t.message ?: "unknown error"}"
            }
        }
    }

    private fun readAvgHeartRateLast24h() {
        if (!ensureSessionAndAvailabilityOrExplain()) return

        lifecycleScope.launch {
            try {
                val app = application as LifeFlowApplication
                val granted = app.getGrantedHealthPermissionsUseCase()
                val required = app.getHealthPermissionsUseCase()

                if (!granted.containsAll(required)) {
                    healthMessage.value = "Missing permissions. Tap Connect Health first."
                    return@launch
                }

                val avg = app.getAvgHeartRateLast24hUseCase()
                avgHrLast24h.value = avg?.roundToLong()

                healthMessage.value =
                    if (avg != null) "Heart rate loaded." else "No heart rate data found (24h)."

            } catch (t: Throwable) {
                healthMessage.value = "Read heart rate failed: ${t.message ?: "unknown error"}"
            }
        }
    }

    private fun resetVaultAndReauth(app: LifeFlowApplication) {
        lifecycleScope.launch {
            try {
                SecurityAccessSession.clear()

                app.androidVault.resetVault()
                app.identityBlobStore.clearAll()
                app.androidVault.ensureInitialized()

                SecurityRuleEngine.setTrustState(
                    TrustState.VERIFIED,
                    reason = "User initiated vault reset"
                )

                // clear wellbeing UI states
                stepsLast24h.value = null
                avgHrLast24h.value = null
                healthMessage.value = "Vault reset complete. Please authenticate again."

                startAuthentication(app)

            } catch (t: Throwable) {
                SecurityAccessSession.clear()
                viewModel.onAuthenticationError("Vault reset failed: ${t.message ?: "unknown error"}")
            }
        }
    }
}