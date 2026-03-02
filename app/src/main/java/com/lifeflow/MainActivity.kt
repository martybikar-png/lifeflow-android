package com.lifeflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import com.lifeflow.data.repository.LocalIdentityRepository
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.EncryptedIdentityRepository
import com.lifeflow.security.EncryptionService
import com.lifeflow.security.KeyManager

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Security setup ---

        val keyManager = KeyManager()
        keyManager.generateKey()

        val encryptionService = EncryptionService(keyManager)

        // ✅ PRODUCTION repository (from :data), not test repo
        val localRepository = LocalIdentityRepository()

        val repository = EncryptedIdentityRepository(
            delegate = localRepository,
            encryptionService = encryptionService
        )

        biometricAuthManager = BiometricAuthManager(this)

        // --- UI state ---

        val uiState = mutableStateOf<UiState>(UiState.Loading)

        setContent {
            when (val state = uiState.value) {
                UiState.Loading -> LoadingScreen()
                UiState.Authenticated -> DashboardScreen()
                is UiState.Error -> ErrorScreen(state.message)
            }
        }

        // --- Biometric auth ---

        biometricAuthManager.authenticate(
            onSuccess = {
                uiState.value = UiState.Authenticated
            },
            onError = { error ->
                uiState.value = UiState.Error(error)
            }
        )

        // NOTE: "repository" is now ready for use (save/load identity) once you wire UI actions.
        // For now, authentication flow stays the same.
    }
}