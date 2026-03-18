package com.lifeflow

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.security.BiometricAuthManager

internal fun FragmentActivity.resolveStartupBindings(
    app: LifeFlowApplication
): StartupBindings {
    val startupReady = app.ensureStartupInitialized()

    if (!startupReady) {
        return StartupBindings(
            startupReady = false,
            viewModel = null,
            biometricAuthManager = null
        )
    }

    return StartupBindings(
        startupReady = true,
        viewModel = ViewModelProvider(
            this,
            app.mainViewModelFactory
        )[MainViewModel::class.java],
        biometricAuthManager = BiometricAuthManager(this)
    )
}

internal data class StartupBindings(
    val startupReady: Boolean,
    val viewModel: MainViewModel?,
    val biometricAuthManager: BiometricAuthManager?
)