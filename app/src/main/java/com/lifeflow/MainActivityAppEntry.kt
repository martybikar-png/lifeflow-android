package com.lifeflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lifeflow.navigation.PublicShellNavHost

private const val EnablePublicShellVisualReview = false
private const val EnableStartupFailureVisualReview = false

@Composable
internal fun AppEntry(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint,
    startupBindings: StartupBindings?,
    initialOnboardingCompleted: Boolean,
    onMarkOnboardingCompleted: () -> Unit,
    appPackageName: String,
    onStartIntent: (android.content.Intent) -> Unit,
    onRecreateActivity: () -> Unit
) {
    var onboardingCompleted by remember {
        mutableStateOf(initialOnboardingCompleted)
    }

    val showPublicShellVisualReview =
        EnablePublicShellVisualReview && shouldUseDebugEmulatorAuthHarness()

    val showStartupFailureVisualReview =
        BuildConfig.DEBUG && EnableStartupFailureVisualReview

    if (showStartupFailureVisualReview) {
        StartupFailureScreen(
            message = "Security startup preview.",
            lastAction = "Visual review only.",
            onRetryStartup = { },
            onOpenAppSettings = { }
        )
        return
    }

    if (showPublicShellVisualReview || !onboardingCompleted) {
        PublicShellNavHost(
            startAtHome = false,
            onOnboardingCompleted = {
                if (!showPublicShellVisualReview) {
                    onMarkOnboardingCompleted()
                    onboardingCompleted = true
                }
            },
            completeOnboardingLocally = showPublicShellVisualReview
        )
        return
    }

    val bindings = startupBindings ?: run {
        IntroSplashScreen()
        return
    }

    if (!bindings.startupReady) {
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
        viewModel = requireNotNull(bindings.viewModel),
        biometricAuthManager = requireNotNull(bindings.biometricAuthManager),
        appPackageName = appPackageName,
        onStartIntent = onStartIntent,
        showIntroSplashOnStart = false
    )
}
