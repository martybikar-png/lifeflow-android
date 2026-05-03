package com.lifeflow.navigation

import androidx.compose.runtime.Composable
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
internal fun ProtectedRuntimeRouteContent(
    activeRoute: String,
    screen: ActiveRuntimeScreenSnapshot,
    onRouteChange: (String) -> Unit,
    onBack: () -> Unit,
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
    when (activeRoute) {
        ProtectedDashboardRoute -> {
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
                    onRouteChange(ProtectedHomeRoute)
                },
                lastAction = screen.lastAction,
                isSessionAuthorized = screen.isAuthenticating
            )
        }

        ProtectedHomeRoute -> {
            HomeScreen(
                onOpenDashboard = {
                    onRouteChange(ProtectedDashboardRoute)
                },
                onOpenQuickCapture = {
                    onRouteChange(ProtectedQuickCaptureRoute)
                },
                onOpenWellbeing = {
                    onRouteChange(ProtectedWellbeingRoute)
                },
                onOpenJournal = {
                    onRouteChange(ProtectedJournalRoute)
                },
                onOpenSettings = {
                    onRouteChange(ProtectedSettingsRoute)
                },
                onOpenTrust = {
                    onRouteChange(ProtectedTrustRoute)
                }
            )
        }

        ProtectedWellbeingRoute -> {
            WellbeingScreen(
                isProtectedSurface = true,
                statusMessage = screen.lastAction,
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }

        ProtectedJournalRoute -> {
            JournalScreen(
                isProtectedSurface = true,
                statusMessage = screen.lastAction,
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }

        ProtectedQuickCaptureRoute -> {
            QuickCaptureScreen(
                statusMessage = screen.lastAction,
                onPrimaryCapture = {
                    onRouteChange(ProtectedCaptureEntryRoute)
                },
                onOpenCaptureLibrary = {
                    onRouteChange(ProtectedCaptureLibraryRoute)
                },
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }

        ProtectedCaptureEntryRoute -> {
            CaptureEntryScreen(
                onSaveCapture = onSaveQuickCapture,
                onBackToQuickCapture = onBack,
                onOpenCaptureLibrary = {
                    onRouteChange(ProtectedCaptureLibraryRoute)
                }
            )
        }

        ProtectedCaptureLibraryRoute -> {
            CaptureLibraryScreen(
                presentation = screen.quickCaptureLibrary,
                statusMessage = screen.lastAction,
                onLoadLibrary = onLoadQuickCaptureLibrary,
                onDeleteCapture = onDeleteQuickCapture,
                onUpdateCapture = onUpdateQuickCapture,
                onBackToQuickCapture = onBack
            )
        }

        ProtectedSettingsRoute -> {
            SettingsScreen(
                onOpenPrivacy = {
                    onRouteChange(ProtectedPrivacyRoute)
                },
                onOpenTrust = {
                    onRouteChange(ProtectedTrustRoute)
                },
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }

        ProtectedPrivacyRoute -> {
            PrivacyScreen(
                onOpenTrust = {
                    onRouteChange(ProtectedTrustRoute)
                },
                onBackToSettings = {
                    onRouteChange(ProtectedSettingsRoute)
                },
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }

        ProtectedTrustRoute -> {
            TrustScreen(
                onOpenSettings = {
                    onRouteChange(ProtectedSettingsRoute)
                },
                onBackToHome = {
                    onRouteChange(ProtectedHomeRoute)
                }
            )
        }
    }
}
