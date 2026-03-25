// Top-level build file where you can add configuration options common to all sub-projects/modules.

fun String.isDisallowedExternalDependencyVersion(): Boolean {
    val normalized = lowercase()
    return contains("+") ||
        normalized == "latest.release" ||
        normalized == "latest.integration" ||
        normalized.contains("snapshot")
}

subprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            val version = requested.version ?: return@eachDependency
            if (version.isDisallowedExternalDependencyVersion()) {
                throw GradleException(
                    "Disallowed dependency version for ${requested.group}:${requested.name}: $version. " +
                        "Use a fixed pinned version from the version catalog or an explicit fixed declaration."
                )
            }
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}
