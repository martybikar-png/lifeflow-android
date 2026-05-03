package com.lifeflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeflow.AuthenticatedDashboardScreen
import com.lifeflow.CaptureEntryScreen
import com.lifeflow.CaptureLibraryScreen
import com.lifeflow.HomeScreen
import com.lifeflow.OnboardingHomeScreen
import com.lifeflow.OnboardingPermissionsScreen
import com.lifeflow.OnboardingPrivacyScreen
import com.lifeflow.OnboardingSelfScreen
import com.lifeflow.OnboardingTrustScreen
import com.lifeflow.OnboardingTuneScreen
import com.lifeflow.OnboardingTwinScreen
import com.lifeflow.OnboardingVoiceScreen
import com.lifeflow.OnboardingWellScreen
import com.lifeflow.OnboardingWelcomeScreen
import com.lifeflow.PrivacyScreen
import com.lifeflow.QuickCaptureScreen
import com.lifeflow.SettingsScreen
import com.lifeflow.TrustScreen
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.publicShellEnrichedCapturePresentation

@Composable
internal fun PublicShellRouteContent(
    modifier: Modifier = Modifier,
    activeRoute: String,
    startAtHome: Boolean,
    onRouteChange: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    Box(modifier = modifier) {
        when (activeRoute) {
            LifeFlowScreenMap.onboardingWelcome.route -> {
                OnboardingWelcomeScreen(
                    onSplashFinished = {
                        onRouteChange(LifeFlowScreenMap.onboardingPermissions.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingPermissions.route -> {
                OnboardingPermissionsScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingPrivacy.route)
                    },
                    onBack = onCompleteOnboarding
                )
            }

            LifeFlowScreenMap.onboardingPrivacy.route -> {
                OnboardingPrivacyScreen(
                    onFinish = {
                        onRouteChange(LifeFlowScreenMap.onboardingTrust.route)
                    },
                    onBack = {
                        onRouteChange(LifeFlowScreenMap.onboardingPermissions.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingTrust.route -> {
                OnboardingTrustScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingWell.route)
                    },
                    onHowPrivacyWorks = {
                        onRouteChange(LifeFlowScreenMap.onboardingPrivacy.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingWell.route -> {
                OnboardingWellScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingTwin.route)
                    },
                    onAnotherTime = {
                        onRouteChange(LifeFlowScreenMap.onboardingTwin.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingTwin.route -> {
                OnboardingTwinScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingHome.route)
                    },
                    onViewSummary = {
                        onRouteChange(LifeFlowScreenMap.onboardingHome.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingHome.route -> {
                OnboardingHomeScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingSelf.route)
                    },
                    onPause = {
                        onRouteChange(LifeFlowScreenMap.home.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingSelf.route -> {
                OnboardingSelfScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingTune.route)
                    },
                    onAnotherTime = {
                        onRouteChange(LifeFlowScreenMap.onboardingTune.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingTune.route -> {
                OnboardingTuneScreen(
                    onContinue = {
                        onRouteChange(LifeFlowScreenMap.onboardingVoice.route)
                    },
                    onLeaveAsIs = {
                        onRouteChange(LifeFlowScreenMap.onboardingVoice.route)
                    }
                )
            }

            LifeFlowScreenMap.onboardingVoice.route -> {
                OnboardingVoiceScreen(
                    onContinue = onCompleteOnboarding,
                    onStayQuiet = onCompleteOnboarding
                )
            }

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
}
