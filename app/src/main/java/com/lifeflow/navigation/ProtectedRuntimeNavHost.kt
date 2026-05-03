package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeflow.ActiveRuntimeScreenSnapshot

internal const val ProtectedDashboardRoute = "protected/dashboard"
internal const val ProtectedHomeRoute = "protected/home"
internal const val ProtectedWellbeingRoute = "protected/wellbeing"
internal const val ProtectedJournalRoute = "protected/journal"
internal const val ProtectedQuickCaptureRoute = "protected/quick-capture"
internal const val ProtectedCaptureEntryRoute = "protected/capture-entry"
internal const val ProtectedCaptureLibraryRoute = "protected/capture-library"
internal const val ProtectedSettingsRoute = "protected/settings"
internal const val ProtectedPrivacyRoute = "protected/privacy"
internal const val ProtectedTrustRoute = "protected/trust"

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
        startDestination = ProtectedDashboardRoute
    ) {
        protectedRoutes.forEach { route ->
            composable(route) {
                ProtectedRuntimeRouteContent(
                    activeRoute = route,
                    screen = screen,
                    onRouteChange = { target ->
                        navController.navigateProtectedSingleTop(target)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaveQuickCapture = onSaveQuickCapture,
                    onLoadQuickCaptureLibrary = onLoadQuickCaptureLibrary,
                    onDeleteQuickCapture = onDeleteQuickCapture,
                    onUpdateQuickCapture = onUpdateQuickCapture,
                    onAuthenticate = onAuthenticate,
                    onGrantHealthPermissions = onGrantHealthPermissions,
                    onOpenHealthConnectSettings = onOpenHealthConnectSettings,
                    onRefreshNow = onRefreshNow,
                    onUpgradeToCore = onUpgradeToCore
                )
            }
        }
    }
}

private val protectedRoutes = listOf(
    ProtectedDashboardRoute,
    ProtectedHomeRoute,
    ProtectedWellbeingRoute,
    ProtectedJournalRoute,
    ProtectedQuickCaptureRoute,
    ProtectedCaptureEntryRoute,
    ProtectedCaptureLibraryRoute,
    ProtectedSettingsRoute,
    ProtectedPrivacyRoute,
    ProtectedTrustRoute
)

private fun NavHostController.navigateProtectedSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
