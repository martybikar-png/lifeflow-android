package com.lifeflow

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.security.BiometricAuthManager

internal fun FragmentActivity.resolveStartupBindings(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint
): StartupBindings {
    val startupReady = startupRuntimeEntryPoint.ensureStarted()

    if (!startupReady) {
        return StartupBindings(
            startupReady = false,
            viewModel = null,
            biometricAuthManager = null
        )
    }

    startupRuntimeEntryPoint.scheduleIntegrityTrustStartupCheck()

    return StartupBindings(
        startupReady = true,
        viewModel = ViewModelProvider(
            this,
            startupRuntimeEntryPoint.requireMainViewModelFactory()
        )[MainViewModel::class.java],
        biometricAuthManager = BiometricAuthManager(
            activity = this,
            authPerUseCryptoProvider = startupRuntimeEntryPoint.authPerUseCryptoProviderOrNull()
        )
    )
}

internal data class StartupBindings(
    val startupReady: Boolean,
    val viewModel: MainViewModel?,
    val biometricAuthManager: BiometricAuthManager?
)
