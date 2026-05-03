package com.lifeflow.navigation

import androidx.compose.runtime.Composable
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
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.publicShellEnrichedCapturePresentation

@Composable
internal fun PublicShellMainRouteContent(
    activeRoute: String,
    startAtHome: Boolean,
    onRouteChange: (String) -> Unit
) {
    when (activeRoute) {
        LifeFlowScreenMap.home.route -> {
            HomeScreen(
                onOpenDashboard = {
                    if (startAtHome) {
                        onRouteChange(PublicShellDashboardPreviewRoute)
                    }
                },
                onOpenQuickCapture = {
                    onRouteChange(LifeFlowScreenMap.quickCapture.route)
                },
                onOpenWellbeing = {
                    onRouteChange(LifeFlowScreenMap.wellbeing.route)
                },
                onOpenJournal = {
                    onRouteChange(LifeFlowScreenMap.journal.route)
                },
                onOpenSettings = {
                    onRouteChange(LifeFlowScreenMap.settings.route)
                },
                onOpenTrust = {
                    onRouteChange(LifeFlowScreenMap.trust.route)
                }
            )
        }

        PublicShellDashboardPreviewRoute -> {
            AuthenticatedDashboardScreen(
                healthState = HealthConnectUiState.Available,
                requiredCount = 0,
                grantedCount = 0,
                stepsGranted = true,
                hrGranted = true,
                digitalTwinState = null,
                wellbeingAssessment = null,
                boundarySnapshot = MainBoundarySnapshot.initial(),
                onRefreshNow = {},
                onGrantHealthPermissions = {},
                onOpenHealthConnectSettings = {},
                onReAuthenticate = {},
                onUpgradeToCore = {},
                onOpenHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                },
                lastAction = "",
                isSessionAuthorized = true
            )
        }

        LifeFlowScreenMap.wellbeing.route -> {
            WellbeingScreen(
                isProtectedSurface = false,
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }

        LifeFlowScreenMap.journal.route -> {
            JournalScreen(
                isProtectedSurface = false,
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }

        LifeFlowScreenMap.quickCapture.route -> {
            QuickCaptureScreen(
                enrichedCapturePresentation = publicShellEnrichedCapturePresentation(),
                onPrimaryCapture = {
                    onRouteChange(LifeFlowScreenMap.captureEntry.route)
                },
                onOpenCaptureLibrary = {
                    onRouteChange(LifeFlowScreenMap.captureLibrary.route)
                },
                onUpgradeToCore = {},
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }

        LifeFlowScreenMap.captureEntry.route -> {
            CaptureEntryScreen(
                onBackToQuickCapture = {
                    onRouteChange(LifeFlowScreenMap.quickCapture.route)
                },
                onOpenCaptureLibrary = {
                    onRouteChange(LifeFlowScreenMap.captureLibrary.route)
                }
            )
        }

        LifeFlowScreenMap.captureLibrary.route -> {
            CaptureLibraryScreen(
                onBackToQuickCapture = {
                    onRouteChange(LifeFlowScreenMap.quickCapture.route)
                }
            )
        }

        LifeFlowScreenMap.trust.route -> {
            TrustScreen(
                onOpenSettings = {
                    onRouteChange(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }

        LifeFlowScreenMap.settings.route -> {
            SettingsScreen(
                onOpenPrivacy = {
                    onRouteChange(LifeFlowScreenMap.privacy.route)
                },
                onOpenTrust = {
                    onRouteChange(LifeFlowScreenMap.trust.route)
                },
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }

        LifeFlowScreenMap.privacy.route -> {
            PrivacyScreen(
                onOpenTrust = {
                    onRouteChange(LifeFlowScreenMap.trust.route)
                },
                onBackToSettings = {
                    onRouteChange(LifeFlowScreenMap.settings.route)
                },
                onBackToHome = {
                    onRouteChange(LifeFlowScreenMap.home.route)
                }
            )
        }
    }
}
