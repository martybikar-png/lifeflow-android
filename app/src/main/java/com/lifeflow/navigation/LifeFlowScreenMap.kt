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
        onboardingPrivacy
    )

    val primaryDestinations = listOf(
        home,
        quickCapture,
        trust,
        settings
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
