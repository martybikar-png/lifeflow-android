package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeflow.ActiveRuntimeScreenSnapshot
import com.lifeflow.AuthenticatedDashboardScreen
import com.lifeflow.HomeScreen
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
            HomeScreen(onOpenQuickCapture = { navController.navigate("protected/quick-capture") { launchSingleTop = true } }, onOpenSettings = { navController.navigate("protected/settings") { launchSingleTop = true } }, onOpenTrust = { navController.navigate("protected/trust") { launchSingleTop = true } })
        }

        composable("protected/quick-capture") { QuickCaptureScreen(onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }

        composable("protected/settings") { SettingsScreen(onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }

        composable("protected/trust") { TrustScreen(onBackToHome = { navController.navigate("protected/home") { launchSingleTop = true } }) }
    }
}
