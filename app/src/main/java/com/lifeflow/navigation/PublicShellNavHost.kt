package com.lifeflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.lifeflow.publicShellEnrichedCapturePresentation

@Composable
internal fun PublicShellNavHost(
    modifier: Modifier = Modifier,
    onOnboardingCompleted: () -> Unit = {},
    completeOnboardingLocally: Boolean = false
) {
    var currentRoute by rememberSaveable {
        mutableStateOf(LifeFlowScreenMap.onboardingWelcome.route)
    }

    val completeOnboarding: () -> Unit = {
        if (completeOnboardingLocally) {
            currentRoute = LifeFlowScreenMap.home.route
        } else {
            onOnboardingCompleted()
        }
    }

    val activeRoute = when (currentRoute) {
        LifeFlowScreenMap.onboardingWelcome.route,
        LifeFlowScreenMap.onboardingPermissions.route,
        LifeFlowScreenMap.onboardingPrivacy.route,
        LifeFlowScreenMap.onboardingTrust.route,
        LifeFlowScreenMap.onboardingWell.route,
        LifeFlowScreenMap.onboardingTwin.route,
        LifeFlowScreenMap.onboardingHome.route,
        LifeFlowScreenMap.onboardingSelf.route,
        LifeFlowScreenMap.onboardingTune.route,
        LifeFlowScreenMap.onboardingVoice.route,
        LifeFlowScreenMap.home.route,
        LifeFlowScreenMap.quickCapture.route,
        LifeFlowScreenMap.captureEntry.route,
        LifeFlowScreenMap.captureLibrary.route,
        LifeFlowScreenMap.trust.route,
        LifeFlowScreenMap.settings.route,
        LifeFlowScreenMap.privacy.route -> currentRoute

        else -> LifeFlowScreenMap.onboardingWelcome.route
    }

    Box(modifier = modifier) {
        when (activeRoute) {
            LifeFlowScreenMap.onboardingWelcome.route -> {
                OnboardingWelcomeScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingPermissions.route
                    },
                    onSkipToHome = {
                        completeOnboarding()
                    }
                )
            }

            LifeFlowScreenMap.onboardingPermissions.route -> {
                OnboardingPermissionsScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingPrivacy.route
                    },
                    onBack = {
                        currentRoute = LifeFlowScreenMap.onboardingWelcome.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingPrivacy.route -> {
                OnboardingPrivacyScreen(
                    onFinish = {
                        currentRoute = LifeFlowScreenMap.onboardingTrust.route
                    },
                    onBack = {
                        currentRoute = LifeFlowScreenMap.onboardingPermissions.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingTrust.route -> {
                OnboardingTrustScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingWell.route
                    },
                    onHowPrivacyWorks = {
                        currentRoute = LifeFlowScreenMap.onboardingPrivacy.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingWell.route -> {
                OnboardingWellScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingTwin.route
                    },
                    onAnotherTime = {
                        currentRoute = LifeFlowScreenMap.onboardingTwin.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingTwin.route -> {
                OnboardingTwinScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingHome.route
                    },
                    onViewSummary = {
                        currentRoute = LifeFlowScreenMap.onboardingHome.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingHome.route -> {
                OnboardingHomeScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingSelf.route
                    },
                    onPause = {
                        currentRoute = LifeFlowScreenMap.home.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingSelf.route -> {
                OnboardingSelfScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingTune.route
                    },
                    onAnotherTime = {
                        currentRoute = LifeFlowScreenMap.onboardingTune.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingTune.route -> {
                OnboardingTuneScreen(
                    onContinue = {
                        currentRoute = LifeFlowScreenMap.onboardingVoice.route
                    },
                    onLeaveAsIs = {
                        currentRoute = LifeFlowScreenMap.onboardingVoice.route
                    }
                )
            }

            LifeFlowScreenMap.onboardingVoice.route -> {
                OnboardingVoiceScreen(
                    onContinue = {
                        completeOnboarding()
                    },
                    onStayQuiet = {
                        completeOnboarding()
                    }
                )
            }

            LifeFlowScreenMap.home.route -> {
                HomeScreen(
                    onOpenQuickCapture = {
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    },
                    onOpenSettings = {
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onOpenTrust = {
                        currentRoute = LifeFlowScreenMap.trust.route
                    }
                )
            }

            LifeFlowScreenMap.quickCapture.route -> {
                QuickCaptureScreen(
                    enrichedCapturePresentation = publicShellEnrichedCapturePresentation(),
                    onPrimaryCapture = {
                        currentRoute = LifeFlowScreenMap.captureEntry.route
                    },
                    onOpenCaptureLibrary = {
                        currentRoute = LifeFlowScreenMap.captureLibrary.route
                    },
                    onUpgradeToCore = {},
                    onBackToHome = {
                        currentRoute = LifeFlowScreenMap.home.route
                    }
                )
            }

            LifeFlowScreenMap.captureEntry.route -> {
                CaptureEntryScreen(
                    onBackToQuickCapture = {
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    }
                )
            }

            LifeFlowScreenMap.captureLibrary.route -> {
                CaptureLibraryScreen(
                    onBackToQuickCapture = {
                        currentRoute = LifeFlowScreenMap.quickCapture.route
                    }
                )
            }

            LifeFlowScreenMap.trust.route -> {
                TrustScreen(
                    onOpenSettings = {
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onBackToHome = {
                        currentRoute = LifeFlowScreenMap.home.route
                    }
                )
            }

            LifeFlowScreenMap.settings.route -> {
                SettingsScreen(
                    onOpenPrivacy = {
                        currentRoute = LifeFlowScreenMap.privacy.route
                    },
                    onOpenTrust = {
                        currentRoute = LifeFlowScreenMap.trust.route
                    },
                    onBackToHome = {
                        currentRoute = LifeFlowScreenMap.home.route
                    }
                )
            }

            LifeFlowScreenMap.privacy.route -> {
                PrivacyScreen(
                    onOpenTrust = {
                        currentRoute = LifeFlowScreenMap.trust.route
                    },
                    onBackToSettings = {
                        currentRoute = LifeFlowScreenMap.settings.route
                    },
                    onBackToHome = {
                        currentRoute = LifeFlowScreenMap.home.route
                    }
                )
            }
        }
    }
}
