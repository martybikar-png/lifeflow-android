package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeflow.HomeScreen
import com.lifeflow.OnboardingPermissionsScreen
import com.lifeflow.OnboardingPrivacyScreen
import com.lifeflow.OnboardingWelcomeScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.QuickCaptureScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.TrustScreen

@Composable
internal fun LifeFlowNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var isOnboardingComplete by rememberSaveable { mutableStateOf(false) }
    var shellLastAction by rememberSaveable {
        mutableStateOf("Shell navigation ready.")
    }

    NavHost(
        navController = navController,
        startDestination = LifeFlowScreenMap.startDestination(isOnboardingComplete),
        modifier = modifier
    ) {
        composable(LifeFlowScreenMap.onboardingWelcome.route) {
            OnboardingWelcomeScreen(
                lastAction = shellLastAction,
                onContinue = {
                    shellLastAction = "Onboarding welcome continued."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.onboardingPermissions.route)
                },
                onSkipToHome = {
                    shellLastAction = "Onboarding skipped to Home shell."
                    isOnboardingComplete = true
                    navController.navigateToHomeClearingOnboarding()
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No final onboarding authority connected"
                )
            )
        }

        composable(LifeFlowScreenMap.onboardingPermissions.route) {
            OnboardingPermissionsScreen(
                lastAction = shellLastAction,
                onContinue = {
                    shellLastAction = "Onboarding permissions continued to privacy."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.onboardingPrivacy.route)
                },
                onBack = {
                    shellLastAction = "Returned to onboarding welcome shell."
                    navController.popBackStack()
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No final permission flow connected"
                )
            )
        }

        composable(LifeFlowScreenMap.onboardingPrivacy.route) {
            OnboardingPrivacyScreen(
                lastAction = shellLastAction,
                onFinish = {
                    shellLastAction = "Onboarding privacy finished. Home shell opened."
                    isOnboardingComplete = true
                    navController.navigateToHomeClearingOnboarding()
                },
                onBack = {
                    shellLastAction = "Returned to onboarding permissions shell."
                    navController.popBackStack()
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No final privacy authority connected"
                )
            )
        }

        composable(LifeFlowScreenMap.home.route) {
            HomeScreen(
                lastAction = shellLastAction,
                onOpenQuickCapture = {
                    shellLastAction = "Quick Capture shell opened from Home."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.quickCapture.route)
                },
                onOpenSettings = {
                    shellLastAction = "Settings shell opened from Home."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onOpenTrust = {
                    shellLastAction = "Trust shell opened from Home."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No protected home orchestration connected"
                )
            )
        }

        composable(LifeFlowScreenMap.quickCapture.route) {
            QuickCaptureScreen(
                lastAction = shellLastAction,
                onPrimaryCapture = {
                    shellLastAction =
                        "Quick Capture shell action triggered. Final capture pipeline is not wired yet."
                },
                onOpenCaptureLibrary = {
                    shellLastAction =
                        "Capture library shell action triggered. Final library flow is not wired yet."
                },
                onBackToHome = {
                    shellLastAction = "Returned to Home from Quick Capture shell."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No protected capture pipeline connected"
                )
            )
        }

        composable(LifeFlowScreenMap.trust.route) {
            TrustScreen(
                lastAction = shellLastAction,
                onOpenSettings = {
                    shellLastAction = "Settings shell opened from Trust."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    shellLastAction = "Returned to Home from Trust shell."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No final trust-state branching connected"
                )
            )
        }

        composable(LifeFlowScreenMap.settings.route) {
            SettingsScreen(
                lastAction = shellLastAction,
                onOpenPrivacy = {
                    shellLastAction = "Privacy shell opened from Settings."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.privacy.route)
                },
                onOpenTrust = {
                    shellLastAction = "Trust shell opened from Settings."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToHome = {
                    shellLastAction = "Returned to Home from Settings shell."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No protected settings logic connected"
                )
            )
        }

        composable(LifeFlowScreenMap.privacy.route) {
            PrivacyScreen(
                lastAction = shellLastAction,
                onOpenTrust = {
                    shellLastAction = "Trust shell opened from Privacy."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToSettings = {
                    shellLastAction = "Returned to Settings from Privacy shell."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    shellLastAction = "Returned to Home from Privacy shell."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No final privacy policy engine connected"
                )
            )
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToHomeClearingOnboarding() {
    navigate(LifeFlowScreenMap.home.route) {
        popUpTo(LifeFlowScreenMap.onboardingWelcome.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
