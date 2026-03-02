package com.lifeflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityAccessSession

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Get repository from Application (single source of truth) ---
        val app = application as LifeFlowApplication

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(app.identityRepository)
        )[MainViewModel::class.java]

        biometricAuthManager = BiometricAuthManager(this)

        // --- UI ---
        setContent {
            when (val state = viewModel.uiState.value) {
                UiState.Loading -> LoadingScreen()
                UiState.Authenticated -> DashboardScreen()
                is UiState.Error -> ErrorScreen(state.message)
            }
        }

        // --- Biometric auth ---
        biometricAuthManager.authenticate(
            onSuccess = {
                SecurityAccessSession.grant(durationMs = 30_000)
                viewModel.onAuthenticationSuccess()
            },
            onError = { error ->
                viewModel.onAuthenticationError(error)
            }
        )
    }
}