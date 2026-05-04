package com.lifeflow

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.security.BiometricAuthManager

internal fun FragmentActivity.resolveStartupBindings(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint
): StartupBindings {
    val startupReady = startupRuntimeEntryPoint.ensureStarted()

    return if (startupReady) {
        resolveStartedStartupBindings(startupRuntimeEntryPoint)
    } else {
        failedStartupBindings()
    }
}

internal fun FragmentActivity.resolveStartedStartupBindings(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint
): StartupBindings {
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

internal fun failedStartupBindings(): StartupBindings {
    return StartupBindings(
        startupReady = false,
        viewModel = null,
        biometricAuthManager = null
    )
}

internal data class StartupBindings(
    val startupReady: Boolean,
    val viewModel: MainViewModel?,
    val biometricAuthManager: BiometricAuthManager?
)
