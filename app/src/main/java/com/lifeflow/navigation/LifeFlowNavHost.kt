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

    NavHost(
        navController = navController,
        startDestination = LifeFlowScreenMap.startDestination(isOnboardingComplete),
        modifier = modifier
    ) {
        composable(LifeFlowScreenMap.onboardingWelcome.route) {
            OnboardingWelcomeScreen(
                lastAction = "Onboarding welcome shell active",
                onContinue = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.onboardingPermissions.route)
                },
                onSkipToHome = {
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
                lastAction = "Onboarding permissions shell active",
                onContinue = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.onboardingPrivacy.route)
                },
                onBack = {
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
                lastAction = "Onboarding privacy shell active",
                onFinish = {
                    isOnboardingComplete = true
                    navController.navigateToHomeClearingOnboarding()
                },
                onBack = {
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
                lastAction = "Home shell active",
                onOpenQuickCapture = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.quickCapture.route)
                },
                onOpenSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onOpenTrust = {
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
                lastAction = "Quick capture shell active",
                onPrimaryCapture = {},
                onOpenCaptureLibrary = {},
                onBackToHome = {
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
                lastAction = "Trust shell active",
                onOpenSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
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
                lastAction = "Settings shell active",
                onOpenPrivacy = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.privacy.route)
                },
                onOpenTrust = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToHome = {
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
                lastAction = "Privacy shell active",
                onOpenTrust = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
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
