package com.lifeflow.navigation

import androidx.compose.runtime.Composable
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
import com.lifeflow.publicShellEnrichedCapturePresentation

@Composable
internal fun LifeFlowNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LifeFlowScreenMap.home.route,
        modifier = modifier
    ) {
        composable(LifeFlowScreenMap.home.route) {
            HomeScreen(
                onOpenQuickCapture = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.quickCapture.route)
                },
                onOpenSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onOpenTrust = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                }
            )
        }

        composable(LifeFlowScreenMap.quickCapture.route) {
            QuickCaptureScreen(
                enrichedCapturePresentation = publicShellEnrichedCapturePresentation(),
                onPrimaryCapture = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.captureEntry.route)
                },
                onOpenCaptureLibrary = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.captureLibrary.route)
                },
                onUpgradeToCore = {},
                onBackToHome = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                }
            )
        }

        composable(LifeFlowScreenMap.captureEntry.route) {
            CaptureEntryScreen(
                onBackToQuickCapture = {
                    navController.popBackStack()
                }
            )
        }

        composable(LifeFlowScreenMap.captureLibrary.route) {
            CaptureLibraryScreen(
                onBackToQuickCapture = {
                    navController.popBackStack()
                }
            )
        }

        composable(LifeFlowScreenMap.trust.route) {
            TrustScreen(
                onOpenSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                }
            )
        }

        composable(LifeFlowScreenMap.settings.route) {
            SettingsScreen(
                onOpenPrivacy = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.privacy.route)
                },
                onOpenTrust = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToHome = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                }
            )
        }

        composable(LifeFlowScreenMap.privacy.route) {
            PrivacyScreen(
                onOpenTrust = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.trust.route)
                },
                onBackToSettings = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    navController.navigateSingleTopTo(LifeFlowScreenMap.home.route)
                }
            )
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
