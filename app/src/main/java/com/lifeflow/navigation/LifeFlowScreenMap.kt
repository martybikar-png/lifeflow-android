package com.lifeflow.navigation

enum class LifeFlowNavSection {
    ONBOARDING,
    HOME,
    CAPTURE,
    TRUST,
    SETTINGS,
    PRIVACY
}

data class LifeFlowDestination(
    val route: String,
    val section: LifeFlowNavSection,
    val title: String,
    val isPrimary: Boolean = false,
    val supportsBack: Boolean = false
)

data class LifeFlowFlowGroup(
    val title: String,
    val destinations: List<LifeFlowDestination>
)

object LifeFlowScreenMap {
    val onboardingWelcome = LifeFlowDestination(
        route = "onboarding/welcome",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Welcome"
    )

    val onboardingPermissions = LifeFlowDestination(
        route = "onboarding/permissions",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Permissions",
        supportsBack = true
    )

    val onboardingPrivacy = LifeFlowDestination(
        route = "onboarding/privacy",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Privacy",
        supportsBack = true
    )

    val onboardingTrust = LifeFlowDestination(
        route = "onboarding/trust",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Trust",
        supportsBack = true
    )

    val onboardingWell = LifeFlowDestination(
        route = "onboarding/well",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Well",
        supportsBack = true
    )

    val onboardingTwin = LifeFlowDestination(
        route = "onboarding/twin",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Twin",
        supportsBack = true
    )

    val onboardingHome = LifeFlowDestination(
        route = "onboarding/home",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Home",
        supportsBack = true
    )

    val onboardingSelf = LifeFlowDestination(
        route = "onboarding/self",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Self",
        supportsBack = true
    )

    val onboardingTune = LifeFlowDestination(
        route = "onboarding/tune",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Tune",
        supportsBack = true
    )

    val onboardingVoice = LifeFlowDestination(
        route = "onboarding/voice",
        section = LifeFlowNavSection.ONBOARDING,
        title = "Voice",
        supportsBack = true
    )

    val home = LifeFlowDestination(
        route = "home",
        section = LifeFlowNavSection.HOME,
        title = "Home",
        isPrimary = true
    )

    val quickCapture = LifeFlowDestination(
        route = "capture",
        section = LifeFlowNavSection.CAPTURE,
        title = "Quick Capture",
        isPrimary = true
    )

    val captureEntry = LifeFlowDestination(
        route = "capture/entry",
        section = LifeFlowNavSection.CAPTURE,
        title = "Capture Entry",
        supportsBack = true
    )

    val captureLibrary = LifeFlowDestination(
        route = "capture/library",
        section = LifeFlowNavSection.CAPTURE,
        title = "Capture Library",
        supportsBack = true
    )

    val trust = LifeFlowDestination(
        route = "trust",
        section = LifeFlowNavSection.TRUST,
        title = "Trust",
        isPrimary = true
    )

    val settings = LifeFlowDestination(
        route = "settings",
        section = LifeFlowNavSection.SETTINGS,
        title = "Settings",
        isPrimary = true
    )

    val privacy = LifeFlowDestination(
        route = "privacy",
        section = LifeFlowNavSection.PRIVACY,
        title = "Privacy",
        supportsBack = true
    )

    val onboardingDestinations = listOf(
        onboardingWelcome,
        onboardingPermissions,
        onboardingPrivacy,
        onboardingTrust,
        onboardingWell,
        onboardingTwin,
        onboardingHome,
        onboardingSelf,
        onboardingTune,
        onboardingVoice
    )

    val primaryDestinations = listOf(
        home,
        quickCapture,
        trust,
        settings
    )

    val captureSupportDestinations = listOf(
        captureEntry,
        captureLibrary
    )

    val supportingDestinations = listOf(
        privacy
    )

    val mvpShellGroups = listOf(
        LifeFlowFlowGroup(
            title = "Onboarding",
            destinations = onboardingDestinations
        ),
        LifeFlowFlowGroup(
            title = "Core shell",
            destinations = primaryDestinations
        ),
        LifeFlowFlowGroup(
            title = "Capture support",
            destinations = captureSupportDestinations
        ),
        LifeFlowFlowGroup(
            title = "Support",
            destinations = supportingDestinations
        )
    )

    val mvpShellDestinations = mvpShellGroups.flatMap { it.destinations }

    val allDestinations = mvpShellDestinations

    fun startDestination(isOnboardingComplete: Boolean): String {
        return if (isOnboardingComplete) {
            home.route
        } else {
            onboardingWelcome.route
        }
    }

    fun find(route: String?): LifeFlowDestination? {
        return allDestinations.firstOrNull { it.route == route }
    }

    fun nextOnboardingDestination(currentRoute: String): LifeFlowDestination? {
        val currentIndex = onboardingDestinations.indexOfFirst { it.route == currentRoute }
        return if (currentIndex == -1 || currentIndex == onboardingDestinations.lastIndex) {
            null
        } else {
            onboardingDestinations[currentIndex + 1]
        }
    }
}
