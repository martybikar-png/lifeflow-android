package com.lifeflow.navigation

internal const val PublicShellDashboardPreviewRoute = "debug/dashboard"

internal fun publicShellActiveRoute(currentRoute: String): String {
    return when (currentRoute) {
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
        LifeFlowScreenMap.privacy.route,
        PublicShellDashboardPreviewRoute -> currentRoute

        else -> LifeFlowScreenMap.onboardingWelcome.route
    }
}
