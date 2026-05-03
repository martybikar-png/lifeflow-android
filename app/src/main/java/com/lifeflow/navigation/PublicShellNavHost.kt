package com.lifeflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun PublicShellNavHost(
    modifier: Modifier = Modifier,
    startAtHome: Boolean = false,
    onOnboardingCompleted: () -> Unit = {},
    completeOnboardingLocally: Boolean = false
) {
    var currentRoute by rememberSaveable {
        mutableStateOf(
            if (startAtHome) {
                LifeFlowScreenMap.home.route
            } else {
                LifeFlowScreenMap.onboardingWelcome.route
            }
        )
    }

    val completeOnboarding: () -> Unit = {
        if (completeOnboardingLocally) {
            currentRoute = LifeFlowScreenMap.home.route
        } else {
            onOnboardingCompleted()
        }
    }

    PublicShellRouteContent(
        modifier = modifier,
        activeRoute = publicShellActiveRoute(currentRoute),
        startAtHome = startAtHome,
        onRouteChange = { route -> currentRoute = route },
        onCompleteOnboarding = completeOnboarding
    )
}
