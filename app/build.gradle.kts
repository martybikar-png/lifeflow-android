import com.android.build.api.variant.HostTestBuilder
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

private val DEBUG_SIGNATURE_PLACEHOLDER = "DEBUG_SIGNATURE_PLACEHOLDER"
private val RELEASE_STORE_FILE_PROPERTY = "lifeflow.releaseStoreFile"
private val RELEASE_STORE_PASSWORD_PROPERTY = "lifeflow.releaseStorePassword"
private val RELEASE_KEY_ALIAS_PROPERTY = "lifeflow.releaseKeyAlias"
private val RELEASE_KEY_PASSWORD_PROPERTY = "lifeflow.releaseKeyPassword"
private val PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER_PROPERTY =
    "lifeflow.playIntegrityCloudProjectNumber"
private val INTEGRITY_TRUST_VERDICT_HOST_PROPERTY =
    "lifeflow.integrityTrustVerdictHost"
private val INTEGRITY_TRUST_VERDICT_PORT_PROPERTY =
    "lifeflow.integrityTrustVerdictPort"
private val INTEGRITY_TRUST_VERDICT_PINNING_ENFORCED_PROPERTY =
    "lifeflow.integrityTrustVerdictPinningEnforced"
private val INTEGRITY_TRUST_VERDICT_PINNED_SPKI_SHA256_SET_PROPERTY =
    "lifeflow.integrityTrustVerdictPinnedSpkiSha256Set"
private val EMERGENCY_AUTHORITY_CONTROL_HOST_PROPERTY =
    "lifeflow.emergencyAuthorityControlHost"
private val EMERGENCY_AUTHORITY_CONTROL_PORT_PROPERTY =
    "lifeflow.emergencyAuthorityControlPort"
private val EMERGENCY_AUTHORITY_AUDIT_HOST_PROPERTY =
    "lifeflow.emergencyAuthorityAuditHost"
private val EMERGENCY_AUTHORITY_AUDIT_PORT_PROPERTY =
    "lifeflow.emergencyAuthorityAuditPort"

fun isReleaseBuildExplicitlyAllowed(): Boolean {
    return providers.gradleProperty("lifeflow.allowReleaseBuild")
        .orNull
        ?.equals("true", ignoreCase = true) == true
}

fun readTextGradleProperty(name: String): String? {
    return providers.gradleProperty(name)
        .orNull
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

fun readLongGradleProperty(name: String): Long? {
    return readTextGradleProperty(name)?.toLongOrNull()
}

fun readIntGradleProperty(name: String): Int? {
    return readTextGradleProperty(name)?.toIntOrNull()
}

fun readBooleanGradleProperty(name: String): Boolean? {
    return when (readTextGradleProperty(name)?.lowercase()) {
        null -> null
        "true" -> true
        "false" -> false
        else -> null
    }
}

fun toBuildConfigString(value: String): String {
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escaped\""
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

fun readRuntimeHost(
    propertyName: String,
    placeholder: String
): String {
    return readTextGradleProperty(propertyName) ?: placeholder
}

fun readRuntimePort(
    propertyName: String,
    defaultValue: Int
): Int {
    return readIntGradleProperty(propertyName) ?: defaultValue
}

fun readRuntimeBoolean(
    propertyName: String,
    defaultValue: Boolean
): Boolean {
    return readBooleanGradleProperty(propertyName) ?: defaultValue
}

fun readRuntimeLong(
    propertyName: String,
    defaultValue: Long
): Long {
    return readLongGradleProperty(propertyName) ?: defaultValue
}

fun readRuntimeString(
    propertyName: String,
    defaultValue: String
): String {
    return readTextGradleProperty(propertyName) ?: defaultValue
}

fun isPlaceholderHost(raw: String?): Boolean {
    val value = raw?.trim().orEmpty()
    return value.isBlank() || value.endsWith(".invalid", ignoreCase = true)
}

fun missingReleaseSigningProperties(): List<String> {
    return buildList {
        if (readTextGradleProperty(RELEASE_STORE_FILE_PROPERTY) == null) {
            add(RELEASE_STORE_FILE_PROPERTY)
        }
        if (readTextGradleProperty(RELEASE_STORE_PASSWORD_PROPERTY) == null) {
            add(RELEASE_STORE_PASSWORD_PROPERTY)
        }
        if (readTextGradleProperty(RELEASE_KEY_ALIAS_PROPERTY) == null) {
            add(RELEASE_KEY_ALIAS_PROPERTY)
        }
        if (readTextGradleProperty(RELEASE_KEY_PASSWORD_PROPERTY) == null) {
            add(RELEASE_KEY_PASSWORD_PROPERTY)
        }
    }
}

fun missingReleaseRuntimeSecurityProperties(): List<String> {
    return buildList {
        if ((readLongGradleProperty(PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER_PROPERTY) ?: 0L) <= 0L) {
            add(PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER_PROPERTY)
        }

        if (isPlaceholderHost(readTextGradleProperty(INTEGRITY_TRUST_VERDICT_HOST_PROPERTY))) {
            add(INTEGRITY_TRUST_VERDICT_HOST_PROPERTY)
        }
        if ((readIntGradleProperty(INTEGRITY_TRUST_VERDICT_PORT_PROPERTY) ?: 0) !in 1..65535) {
            add(INTEGRITY_TRUST_VERDICT_PORT_PROPERTY)
        }

        if (readBooleanGradleProperty(INTEGRITY_TRUST_VERDICT_PINNING_ENFORCED_PROPERTY) == true) {
            if (readTextGradleProperty(INTEGRITY_TRUST_VERDICT_PINNED_SPKI_SHA256_SET_PROPERTY) == null) {
                add(INTEGRITY_TRUST_VERDICT_PINNED_SPKI_SHA256_SET_PROPERTY)
            }
        }

        if (isPlaceholderHost(readTextGradleProperty(EMERGENCY_AUTHORITY_CONTROL_HOST_PROPERTY))) {
            add(EMERGENCY_AUTHORITY_CONTROL_HOST_PROPERTY)
        }
        if ((readIntGradleProperty(EMERGENCY_AUTHORITY_CONTROL_PORT_PROPERTY) ?: 0) !in 1..65535) {
            add(EMERGENCY_AUTHORITY_CONTROL_PORT_PROPERTY)
        }

        if (isPlaceholderHost(readTextGradleProperty(EMERGENCY_AUTHORITY_AUDIT_HOST_PROPERTY))) {
            add(EMERGENCY_AUTHORITY_AUDIT_HOST_PROPERTY)
        }
        if ((readIntGradleProperty(EMERGENCY_AUTHORITY_AUDIT_PORT_PROPERTY) ?: 0) !in 1..65535) {
            add(EMERGENCY_AUTHORITY_AUDIT_PORT_PROPERTY)
        }
    }
}

android {
    namespace = "com.lifeflow"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val releaseStoreFilePath = readTextGradleProperty(RELEASE_STORE_FILE_PROPERTY)
            val releaseStorePassword = readTextGradleProperty(RELEASE_STORE_PASSWORD_PROPERTY)
            val releaseKeyAlias = readTextGradleProperty(RELEASE_KEY_ALIAS_PROPERTY)
            val releaseKeyPassword = readTextGradleProperty(RELEASE_KEY_PASSWORD_PROPERTY)

            if (
                releaseStoreFilePath != null &&
                releaseStorePassword != null &&
                releaseKeyAlias != null &&
                releaseKeyPassword != null
            ) {
                storeFile = file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                storeType = "pkcs12"
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

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

        buildConfigField(
            "long",
            "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER",
            "${readRuntimeLong(PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER_PROPERTY, 0L)}L"
        )

        buildConfigField(
            "String",
            "INTEGRITY_TRUST_VERDICT_HOST",
            toBuildConfigString(
                readRuntimeHost(
                    INTEGRITY_TRUST_VERDICT_HOST_PROPERTY,
                    "integrity-trust.invalid"
                )
            )
        )
        buildConfigField(
            "int",
            "INTEGRITY_TRUST_VERDICT_PORT",
            readRuntimePort(
                INTEGRITY_TRUST_VERDICT_PORT_PROPERTY,
                443
            ).toString()
        )
        buildConfigField("boolean", "INTEGRITY_TRUST_VERDICT_KEEPALIVE_ENABLED", "false")
        buildConfigField("long", "INTEGRITY_TRUST_VERDICT_KEEPALIVE_TIME_MS", "60000L")
        buildConfigField("long", "INTEGRITY_TRUST_VERDICT_KEEPALIVE_TIMEOUT_MS", "20000L")
        buildConfigField("boolean", "INTEGRITY_TRUST_VERDICT_KEEPALIVE_WITHOUT_CALLS", "false")
        buildConfigField(
            "boolean",
            "INTEGRITY_TRUST_VERDICT_PINNING_ENFORCED",
            readRuntimeBoolean(
                INTEGRITY_TRUST_VERDICT_PINNING_ENFORCED_PROPERTY,
                false
            ).toString()
        )
        buildConfigField(
            "String",
            "INTEGRITY_TRUST_VERDICT_PINNED_SPKI_SHA256_SET",
            toBuildConfigString(
                readRuntimeString(
                    INTEGRITY_TRUST_VERDICT_PINNED_SPKI_SHA256_SET_PROPERTY,
                    ""
                )
            )
        )

        buildConfigField(
            "String",
            "EMERGENCY_AUTHORITY_CONTROL_HOST",
            toBuildConfigString(
                readRuntimeHost(
                    EMERGENCY_AUTHORITY_CONTROL_HOST_PROPERTY,
                    "authority-control.invalid"
                )
            )
        )
        buildConfigField(
            "int",
            "EMERGENCY_AUTHORITY_CONTROL_PORT",
            readRuntimePort(
                EMERGENCY_AUTHORITY_CONTROL_PORT_PROPERTY,
                443
            ).toString()
        )
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_ENABLED", "false")
        buildConfigField("long", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIME_MS", "60000L")
        buildConfigField("long", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_TIMEOUT_MS", "20000L")
        buildConfigField("boolean", "EMERGENCY_AUTHORITY_CONTROL_KEEPALIVE_WITHOUT_CALLS", "false")

        buildConfigField(
            "String",
            "EMERGENCY_AUTHORITY_AUDIT_HOST",
            toBuildConfigString(
                readRuntimeHost(
                    EMERGENCY_AUTHORITY_AUDIT_HOST_PROPERTY,
                    "authority-audit.invalid"
                )
            )
        )
        buildConfigField(
            "int",
            "EMERGENCY_AUTHORITY_AUDIT_PORT",
            readRuntimePort(
                EMERGENCY_AUTHORITY_AUDIT_PORT_PROPERTY,
                443
            ).toString()
        )
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
            buildConfigField(
                "String",
                "EXPECTED_RELEASE_SIGNATURE_SHA256",
                "\"$DEBUG_SIGNATURE_PLACEHOLDER\""
            )
        }

        release {
            signingConfig = signingConfigs.getByName("release")
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

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        variantBuilder.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = false
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

            val missingSigningProperties = missingReleaseSigningProperties()
            if (missingSigningProperties.isNotEmpty()) {
                throw GradleException(
                    "Release signing config is incomplete. Missing properties: ${missingSigningProperties.joinToString(", ")}"
                )
            }

            val missingRuntimeSecurityProperties = missingReleaseRuntimeSecurityProperties()
            if (missingRuntimeSecurityProperties.isNotEmpty()) {
                throw GradleException(
                    "Release runtime security config is incomplete. Missing or invalid properties: ${missingRuntimeSecurityProperties.joinToString(", ")}"
                )
            }

            val releaseStoreFilePath = readTextGradleProperty(RELEASE_STORE_FILE_PROPERTY)!!
            val releaseStoreFile = file(releaseStoreFilePath)
            if (!releaseStoreFile.exists()) {
                throw GradleException(
                    "Release keystore file was not found at: $releaseStoreFilePath"
                )
            }
            if (!releaseStoreFile.isFile) {
                throw GradleException(
                    "Release keystore path is not a file: $releaseStoreFilePath"
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

val verifyReleaseR8Artifacts by tasks.registering {
    dependsOn("minifyReleaseWithR8")

    doLast {
        val releaseMappingDir = layout.buildDirectory.dir("outputs/mapping/release").get().asFile
        val requiredFiles = listOf(
            "mapping.txt",
            "usage.txt",
            "seeds.txt",
            "configuration.txt"
        )

        val missingOrEmpty = requiredFiles.filter { fileName ->
            val file = releaseMappingDir.resolve(fileName)
            !file.exists() || file.length() <= 0L
        }

        if (missingOrEmpty.isNotEmpty()) {
            throw GradleException(
                "Release R8 artifacts missing or empty in ${releaseMappingDir.absolutePath}: ${missingOrEmpty.joinToString(", ")}"
            )
        }

        println("Verified release R8 artifacts:")
        requiredFiles.forEach { fileName ->
            val file = releaseMappingDir.resolve(fileName)
            println(" - $fileName => ${file.absolutePath}")
        }
    }
}

val verifySelectedSecurityObfuscation by tasks.registering {
    dependsOn("verifyReleaseR8Artifacts")

    doLast {
        val mappingFile = layout.buildDirectory.file("outputs/mapping/release/mapping.txt").get().asFile
        val seedsFile = layout.buildDirectory.file("outputs/mapping/release/seeds.txt").get().asFile

        val mappingText = mappingFile.readText()
        val seedsText = seedsFile.readText()

        val classesToVerify = listOf(
            "com.lifeflow.security.KeyManager",
            "com.lifeflow.security.SecurityKeyAttestationBootstrap",
            "com.lifeflow.security.hardening.SecurityHardeningGuard",
            "com.lifeflow.security.integrity.PlayIntegrityVerifier",
            "com.lifeflow.security.GrpcEmergencyAuthorityTransport",
            "com.lifeflow.security.GrpcIntegrityTrustTransport",
            "com.lifeflow.security.IntegrityTrustTlsMaterialProvider",
            "com.lifeflow.security.EncryptionService"
        )

        val seeded = mutableListOf<String>()
        val notObfuscated = mutableListOf<String>()
        val notes = mutableListOf<String>()

        classesToVerify.forEach { className ->
            val exactMapping = Regex(
                "^" + Regex.escape(className) + " -> ([^:]+):$",
                RegexOption.MULTILINE
            ).find(mappingText)

            val mappedName = exactMapping?.groupValues?.get(1)
            if (mappedName != null) {
                if (mappedName.startsWith("com.lifeflow")) {
                    notObfuscated += "$className -> $mappedName"
                } else {
                    notes += "$className -> $mappedName"
                }
            } else {
                notes += "$className -> [no exact class mapping line]"
            }

            if (Regex("(?m)^" + Regex.escape(className) + "($|[:\\s])").containsMatchIn(seedsText)) {
                seeded += className
            }
        }

        if (seeded.isNotEmpty() || notObfuscated.isNotEmpty()) {
            val problems = buildList {
                if (seeded.isNotEmpty()) {
                    add("Classes unexpectedly present in seeds.txt: ${seeded.joinToString(", ")}")
                }
                if (notObfuscated.isNotEmpty()) {
                    add("Classes with readable mapping targets: ${notObfuscated.joinToString(", ")}")
                }
            }

            throw GradleException(
                "Selected security obfuscation verification failed. ${problems.joinToString(" | ")}"
            )
        }

        println("Verified selected security obfuscation / keep posture:")
        notes.forEach { println(" - $it") }
    }
}

val verifyReleaseSecurityBaseline by tasks.registering {
    dependsOn("verifySelectedSecurityObfuscation")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.health.connect.client)
    implementation(libs.org.json)
    implementation(libs.androidx.biometric)
    implementation(libs.play.integrity)

    implementation(libs.play.services.base)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
