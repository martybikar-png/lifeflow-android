import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

private val DEBUG_SIGNATURE_PLACEHOLDER = "DEBUG_SIGNATURE_PLACEHOLDER"

fun isReleaseBuildExplicitlyAllowed(): Boolean {
    return providers.gradleProperty("lifeflow.allowReleaseBuild")
        .orNull
        ?.equals("true", ignoreCase = true) == true
}

fun normalizeSha256Fingerprint(raw: String?): String? {
    val normalized = raw
        ?.trim()
        ?.replace(":", "")
        ?.uppercase()
        ?: return null

    return if (Regex("^[0-9A-F]{64}$").matches(normalized)) normalized else null
}

fun readReleaseSignatureSha256(): String? {
    return normalizeSha256Fingerprint(
        providers.gradleProperty("lifeflow.releaseSignatureSha256").orNull
    )
}

android {
    namespace = "com.lifeflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lifeflow"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "EXPECTED_RELEASE_SIGNATURE_SHA256",
            "\"$DEBUG_SIGNATURE_PLACEHOLDER\""
        )

        buildConfigField("String", "EMERGENCY_AUTHORITY_CONTROL_HOST", "\"authority-control.invalid\"")
        buildConfigField("int", "EMERGENCY_AUTHORITY_CONTROL_PORT", "443")
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_ENABLED", "false")
        buildConfigField("long", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIME_MS", "60000L")
        buildConfigField("long", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIMEOUT_MS", "20000L")
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_WITHOUT_CALLS", "false")

        buildConfigField("String", "EMERGENCY_AUTHORITY_AUDIT_HOST", "\"authority-audit.invalid\"")
        buildConfigField("int", "EMERGENCY_AUTHORITY_AUDIT_PORT", "443")
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_ENABLED", "false")
        buildConfigField("long", "EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_TIME_MS", "60000L")
        buildConfigField("long", "EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_TIMEOUT_MS", "20000L")
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_AUDIT_KEEPALIVE_WITHOUT_CALLS", "false")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
            buildConfigField(
                "String",
                "EXPECTED_RELEASE_SIGNATURE_SHA256",
                "\"$DEBUG_SIGNATURE_PLACEHOLDER\""
            )
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "EXPECTED_RELEASE_SIGNATURE_SHA256",
                "\"${readReleaseSignatureSha256() ?: DEBUG_SIGNATURE_PLACEHOLDER}\""
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            if (!isReleaseBuildExplicitlyAllowed()) {
                throw GradleException(
                    "Release build is blocked by default. Re-run with -Plifeflow.allowReleaseBuild=true when a release build is explicitly intended."
                )
            }

            if (readReleaseSignatureSha256() == null) {
                throw GradleException(
                    "Release signing SHA-256 is missing or invalid. Re-run with -Plifeflow.releaseSignatureSha256=<64 hex chars from Play app signing key certificate>."
                )
            }
        }
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

dependencies {
    // Clean Architecture modules
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    // Health Connect
    implementation(libs.androidx.health.connect.client)

    // JSON
    implementation(libs.org.json)

    // Biometric
    implementation(libs.androidx.biometric)
    implementation(libs.play.integrity)

    // External authority transport foundation
    implementation(libs.play.services.base)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumentation testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
