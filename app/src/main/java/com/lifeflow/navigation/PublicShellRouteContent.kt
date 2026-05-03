package com.lifeflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
            LifeFlowScreenMap.onboardingWelcome.route,
            LifeFlowScreenMap.onboardingPermissions.route,
            LifeFlowScreenMap.onboardingPrivacy.route,
            LifeFlowScreenMap.onboardingTrust.route,
            LifeFlowScreenMap.onboardingWell.route,
            LifeFlowScreenMap.onboardingTwin.route,
            LifeFlowScreenMap.onboardingHome.route,
            LifeFlowScreenMap.onboardingSelf.route,
            LifeFlowScreenMap.onboardingTune.route,
            LifeFlowScreenMap.onboardingVoice.route -> {
                PublicShellOnboardingRouteContent(
                    activeRoute = activeRoute,
                    onRouteChange = onRouteChange,
                    onCompleteOnboarding = onCompleteOnboarding
                )
            }

            else -> {
                PublicShellMainRouteContent(
                    activeRoute = activeRoute,
                    startAtHome = startAtHome,
                    onRouteChange = onRouteChange
                )
            }
        }
    }
}
