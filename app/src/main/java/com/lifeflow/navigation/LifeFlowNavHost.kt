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
import com.lifeflow.CaptureEntryScreen
import com.lifeflow.CaptureLibraryScreen
import com.lifeflow.HomeScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.QuickCaptureScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.TrustScreen

@Composable
internal fun LifeFlowNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var shellLastAction by rememberSaveable {
        mutableStateOf("Shell navigation ready.")
    }

    NavHost(
        navController = navController,
        startDestination = LifeFlowScreenMap.home.route,
        modifier = modifier
    ) {
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
                    shellLastAction = "Capture Entry shell opened from Quick Capture."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.captureEntry.route)
                },
                onOpenCaptureLibrary = {
                    shellLastAction = "Capture Library shell opened from Quick Capture."
                    navController.navigateSingleTopTo(LifeFlowScreenMap.captureLibrary.route)
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

        composable(LifeFlowScreenMap.captureEntry.route) {
            CaptureEntryScreen(
                lastAction = shellLastAction,
                onBackToQuickCapture = {
                    shellLastAction = "Returned to Quick Capture from Capture Entry shell."
                    navController.popBackStack()
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No protected capture entry connected"
                )
            )
        }

        composable(LifeFlowScreenMap.captureLibrary.route) {
            CaptureLibraryScreen(
                lastAction = shellLastAction,
                onBackToQuickCapture = {
                    shellLastAction = "Returned to Quick Capture from Capture Library shell."
                    navController.popBackStack()
                },
                debugLines = listOf(
                    "Shell mode active",
                    "No protected capture library connected"
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
