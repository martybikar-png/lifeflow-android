package com.lifeflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lifeflow.CaptureEntryScreen
import com.lifeflow.CaptureLibraryScreen
import com.lifeflow.HomeScreen
import com.lifeflow.OnboardingPermissionsScreen
import com.lifeflow.OnboardingPrivacyScreen
import com.lifeflow.OnboardingWelcomeScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.QuickCaptureScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.TrustScreen

@Composable
internal fun PublicShellNavHost(
    modifier: Modifier = Modifier,
    onEnterProtectedFlow: () -> Unit = {}
) {
    var shellLastAction by rememberSaveable {
        mutableStateOf("Public shell navigation ready.")
    }

    var currentRoute by rememberSaveable {
        mutableStateOf(LifeFlowScreenMap.onboardingWelcome.route)
    }

    val activeRoute = when (currentRoute) {
        LifeFlowScreenMap.onboardingWelcome.route,
        LifeFlowScreenMap.onboardingPermissions.route,
        LifeFlowScreenMap.onboardingPrivacy.route,
        LifeFlowScreenMap.home.route,
        LifeFlowScreenMap.quickCapture.route,
        LifeFlowScreenMap.captureEntry.route,
        LifeFlowScreenMap.captureLibrary.route,
        LifeFlowScreenMap.trust.route,
        LifeFlowScreenMap.settings.route,
        LifeFlowScreenMap.privacy.route -> currentRoute

        else -> LifeFlowScreenMap.onboardingWelcome.route
    }

    Box(modifier = modifier) {
        when (activeRoute) {
            LifeFlowScreenMap.onboardingWelcome.route -> {
                OnboardingWelcomeScreen(
                    lastAction = shellLastAction,
                    onContinue = {
                        shellLastAction = "Onboarding welcome continued to permissions."
                        currentRoute = LifeFlowScreenMap.onboardingPermissions.route
                    },
                    onSkipToHome = {
                        shellLastAction = "Public onboarding skipped to Home shell."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow not entered yet",
                        "Active shell route: ${LifeFlowScreenMap.onboardingWelcome.route}"
                    )
                )
            }

            LifeFlowScreenMap.onboardingPermissions.route -> {
                OnboardingPermissionsScreen(
                    lastAction = shellLastAction,
                    onContinue = {
                        shellLastAction = "Onboarding permissions continued to privacy."
                        currentRoute = LifeFlowScreenMap.onboardingPrivacy.route
                    },
                    onBack = {
                        shellLastAction = "Returned to onboarding welcome shell."
                        currentRoute = LifeFlowScreenMap.onboardingWelcome.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow not entered yet",
                        "Active shell route: ${LifeFlowScreenMap.onboardingPermissions.route}"
                    )
                )
            }

            LifeFlowScreenMap.onboardingPrivacy.route -> {
                OnboardingPrivacyScreen(
                    lastAction = shellLastAction,
                    onFinish = {
                        shellLastAction = "Public onboarding finished. Home shell opened."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    onBack = {
                        shellLastAction = "Returned to onboarding permissions shell."
                        currentRoute = LifeFlowScreenMap.onboardingPermissions.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow not entered yet",
                        "Active shell route: ${LifeFlowScreenMap.onboardingPrivacy.route}"
                    )
                )
            }

            LifeFlowScreenMap.home.route -> {
                HomeScreen(
                    lastAction = shellLastAction,
                    onOpenQuickCapture = {
                        shellLastAction = "Quick Capture shell opened from Home."
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    },
                    onOpenSettings = {
                        shellLastAction = "Settings shell opened from Home."
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onOpenTrust = {
                        shellLastAction = "Trust shell opened from Home."
                        currentRoute = LifeFlowScreenMap.trust.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.home.route}"
                    )
                )
            }

            LifeFlowScreenMap.quickCapture.route -> {
                QuickCaptureScreen(
                    lastAction = shellLastAction,
                    onPrimaryCapture = {
                        shellLastAction = "Capture Entry shell opened from Quick Capture."
                        currentRoute = LifeFlowScreenMap.captureEntry.route
                    },
                    onOpenCaptureLibrary = {
                        shellLastAction = "Capture Library shell opened from Quick Capture."
                        currentRoute = LifeFlowScreenMap.captureLibrary.route
                    },
                    onBackToHome = {
                        shellLastAction = "Returned to Home from Quick Capture shell."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.quickCapture.route}"
                    )
                )
            }

            LifeFlowScreenMap.captureEntry.route -> {
                CaptureEntryScreen(
                    lastAction = shellLastAction,
                    onBackToQuickCapture = {
                        shellLastAction = "Returned to Quick Capture from Capture Entry shell."
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.captureEntry.route}"
                    )
                )
            }

            LifeFlowScreenMap.captureLibrary.route -> {
                CaptureLibraryScreen(
                    lastAction = shellLastAction,
                    onBackToQuickCapture = {
                        shellLastAction = "Returned to Quick Capture from Capture Library shell."
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.captureLibrary.route}"
                    )
                )
            }

            LifeFlowScreenMap.trust.route -> {
                TrustScreen(
                    lastAction = shellLastAction,
                    onOpenSettings = {
                        shellLastAction = "Settings shell opened from Trust."
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onBackToHome = {
                        shellLastAction = "Returned to Home from Trust shell."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.trust.route}"
                    )
                )
            }

            LifeFlowScreenMap.settings.route -> {
                SettingsScreen(
                    lastAction = shellLastAction,
                    onOpenPrivacy = {
                        shellLastAction = "Privacy shell opened from Settings."
                        currentRoute = LifeFlowScreenMap.privacy.route
                    },
                    onOpenTrust = {
                        shellLastAction = "Trust shell opened from Settings."
                        currentRoute = LifeFlowScreenMap.trust.route
                    },
                    onBackToHome = {
                        shellLastAction = "Returned to Home from Settings shell."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.settings.route}"
                    )
                )
            }

            LifeFlowScreenMap.privacy.route -> {
                PrivacyScreen(
                    lastAction = shellLastAction,
                    onOpenTrust = {
                        shellLastAction = "Trust shell opened from Privacy."
                        currentRoute = LifeFlowScreenMap.trust.route
                    },
                    onBackToSettings = {
                        shellLastAction = "Returned to Settings from Privacy shell."
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onBackToHome = {
                        shellLastAction = "Returned to Home from Privacy shell."
                        currentRoute = LifeFlowScreenMap.home.route
                    },
                    debugLines = listOf(
                        "Public shell mode active",
                        "Protected flow bypassed for shell-only navigation",
                        "Active shell route: ${LifeFlowScreenMap.privacy.route}"
                    )
                )
            }
        }
    }
}
