package com.lifeflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lifeflow.navigation.PublicShellNavHost

@Composable
internal fun AppEntry(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint,
    startupBindings: StartupBindings,
    initialOnboardingCompleted: Boolean,
    onMarkOnboardingCompleted: () -> Unit,
    appPackageName: String,
    onStartIntent: (android.content.Intent) -> Unit,
    onRecreateActivity: () -> Unit
) {
    var onboardingCompleted by remember {
        mutableStateOf(initialOnboardingCompleted)
    }

    if (!onboardingCompleted) {
        PublicShellNavHost(
            onOnboardingCompleted = {
                onMarkOnboardingCompleted()
                onboardingCompleted = true
            }
        )
        return
    }

    if (!startupBindings.startupReady) {
        StartupFailureContent(
            initialStartupFailureMessage = readStartupFailureMessage(startupRuntimeEntryPoint),
            retryStartup = {
                tryEnsureStartupInitialized(startupRuntimeEntryPoint)
            },
            readStartupFailureMessage = {
                readStartupFailureMessage(startupRuntimeEntryPoint)
            },
            appPackageName = appPackageName,
            onStartIntent = onStartIntent,
            onRecreateActivity = onRecreateActivity
        )
        return
    }

    ActiveRuntimeContent(
        viewModel = requireNotNull(startupBindings.viewModel),
        biometricAuthManager = requireNotNull(startupBindings.biometricAuthManager),
        appPackageName = appPackageName,
        onStartIntent = onStartIntent
    )
}
