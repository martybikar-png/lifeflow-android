package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeflow.ActiveRuntimeScreenSnapshot
import com.lifeflow.AuthenticatedDashboardScreen
import com.lifeflow.HomeScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.CaptureLibraryScreen
import com.lifeflow.CaptureEntryScreen
import com.lifeflow.TrustScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.QuickCaptureScreen

@Composable
internal fun ProtectedRuntimeNavHost(
    screen: ActiveRuntimeScreenSnapshot,
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
                onOpenHome = { navController.navigate("protected/home") { launchSingleTop = true } },
                lastAction = screen.lastAction,
                isSessionAuthorized = screen.isAuthenticating
            )
        }

        composable("protected/home") {
            HomeScreen(onOpenDashboard = { navController.navigate("protected/dashboard") { launchSingleTop = true } }, onOpenQuickCapture = { navController.navigate("protected/quick-capture") { launchSingleTop = true } }, onOpenSettings = { navController.navigate("protected/settings") { launchSingleTop = true } }, onOpenTrust = { navController.navigate("protected/trust") { launchSingleTop = true } })
        }

        composable("protected/quick-capture") { QuickCaptureScreen(onPrimaryCapture = { navController.navigate("protected/capture-entry") { launchSingleTop = true } }, onOpenCaptureLibrary = { navController.navigate("protected/capture-library") { launchSingleTop = true } }, onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }

        composable("protected/capture-entry") { CaptureEntryScreen(onBackToQuickCapture = { navController.popBackStack() }) }

        composable("protected/capture-library") { CaptureLibraryScreen(onBackToQuickCapture = { navController.popBackStack() }) }

        composable("protected/settings") { SettingsScreen(onOpenPrivacy = { navController.navigate("protected/privacy") { launchSingleTop = true } }, onOpenTrust = { navController.navigate("protected/trust") { launchSingleTop = true } }, onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }

        composable("protected/privacy") { PrivacyScreen(onOpenTrust = { navController.navigate("protected/trust") { launchSingleTop = true } }, onBackToSettings = { navController.navigate("protected/settings") { launchSingleTop = true } }, onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }

        composable("protected/trust") { TrustScreen(onOpenSettings = { navController.navigate("protected/settings") { launchSingleTop = true } }, onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }
    }
}
