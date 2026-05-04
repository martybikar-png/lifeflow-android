package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import com.lifeflow.OnboardingHomeScreen
import com.lifeflow.OnboardingPermissionsScreen
import com.lifeflow.OnboardingPrivacyScreen
import com.lifeflow.OnboardingSelfScreen
import com.lifeflow.OnboardingTrustScreen
import com.lifeflow.OnboardingTuneScreen
import com.lifeflow.OnboardingTwinScreen
import com.lifeflow.OnboardingVoiceScreen
import com.lifeflow.OnboardingWellScreen

@Composable
internal fun PublicShellOnboardingRouteContent(
    activeRoute: String,
    onRouteChange: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    when (activeRoute) {
        LifeFlowScreenMap.onboardingWelcome.route,
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
    }
}
