package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeflow.ActiveRuntimeScreenSnapshot
import com.lifeflow.AuthenticatedDashboardScreen
import com.lifeflow.CaptureEntryScreen
import com.lifeflow.CaptureLibraryScreen
import com.lifeflow.HomeScreen
import com.lifeflow.JournalScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.QuickCaptureScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.TrustScreen
import com.lifeflow.WellbeingScreen

@Composable
internal fun ProtectedRuntimeNavHost(
    screen: ActiveRuntimeScreenSnapshot,
    onSaveQuickCapture: (String) -> Unit,
    onLoadQuickCaptureLibrary: () -> Unit,
    onDeleteQuickCapture: (String) -> Unit,
    onUpdateQuickCapture: (String, String) -> Unit,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onRefreshNow: () -> Unit,
    onUpgradeToCore: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "protected/dashboard"
    ) {
        composable("protected/dashboard") {
            AuthenticatedDashboardScreen(
                healthState = screen.healthState,
                requiredCount = screen.requiredPermissions.size,
                grantedCount = screen.grantedPermissions.size,
                stepsGranted = screen.stepsGranted,
                hrGranted = screen.hrGranted,
                digitalTwinState = screen.digitalTwinState,
                wellbeingAssessment = screen.wellbeingAssessment,
                boundarySnapshot = screen.boundarySnapshot,
                onRefreshNow = onRefreshNow,
                onGrantHealthPermissions = onGrantHealthPermissions,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                onReAuthenticate = onAuthenticate,
                onUpgradeToCore = onUpgradeToCore,
                onOpenHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                },
                lastAction = screen.lastAction,
                isSessionAuthorized = screen.isAuthenticating
            )
        }

        composable("protected/home") {
            HomeScreen(
                onOpenDashboard = {
                    navController.navigateProtectedSingleTop("protected/dashboard")
                },
                onOpenQuickCapture = {
                    navController.navigateProtectedSingleTop("protected/quick-capture")
                },
                onOpenWellbeing = {
                    navController.navigateProtectedSingleTop("protected/wellbeing")
                },
                onOpenJournal = {
                    navController.navigateProtectedSingleTop("protected/journal")
                },
                onOpenSettings = {
                    navController.navigateProtectedSingleTop("protected/settings")
                },
                onOpenTrust = {
                    navController.navigateProtectedSingleTop("protected/trust")
                }
            )
        }

        composable("protected/wellbeing") {
            WellbeingScreen(
                isProtectedSurface = true,
                statusMessage = screen.lastAction,
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }

        composable("protected/journal") {
            JournalScreen(
                isProtectedSurface = true,
                statusMessage = screen.lastAction,
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }

        composable("protected/quick-capture") {
            QuickCaptureScreen(
                statusMessage = screen.lastAction,
                onPrimaryCapture = {
                    navController.navigateProtectedSingleTop("protected/capture-entry")
                },
                onOpenCaptureLibrary = {
                    navController.navigateProtectedSingleTop("protected/capture-library")
                },
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }

        composable("protected/capture-entry") {
            CaptureEntryScreen(
                onSaveCapture = onSaveQuickCapture,
                onBackToQuickCapture = {
                    navController.popBackStack()
                },
                onOpenCaptureLibrary = {
                    navController.navigateProtectedSingleTop("protected/capture-library")
                }
            )
        }

        composable("protected/capture-library") {
            CaptureLibraryScreen(
                presentation = screen.quickCaptureLibrary,
                statusMessage = screen.lastAction,
                onLoadLibrary = onLoadQuickCaptureLibrary,
                onDeleteCapture = onDeleteQuickCapture,
                onUpdateCapture = onUpdateQuickCapture,
                onBackToQuickCapture = {
                    navController.popBackStack()
                }
            )
        }

        composable("protected/settings") {
            SettingsScreen(
                onOpenPrivacy = {
                    navController.navigateProtectedSingleTop("protected/privacy")
                },
                onOpenTrust = {
                    navController.navigateProtectedSingleTop("protected/trust")
                },
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }

        composable("protected/privacy") {
            PrivacyScreen(
                onOpenTrust = {
                    navController.navigateProtectedSingleTop("protected/trust")
                },
                onBackToSettings = {
                    navController.navigateProtectedSingleTop("protected/settings")
                },
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }

        composable("protected/trust") {
            TrustScreen(
                onOpenSettings = {
                    navController.navigateProtectedSingleTop("protected/settings")
                },
                onBackToHome = {
                    navController.navigateProtectedSingleTop("protected/home")
                }
            )
        }
    }
}

private fun androidx.navigation.NavHostController.navigateProtectedSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
