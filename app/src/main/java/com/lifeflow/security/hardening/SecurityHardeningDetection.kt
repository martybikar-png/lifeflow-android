package com.lifeflow.security.hardening

import android.content.Context
import android.os.Build
import com.lifeflow.BuildConfig
import java.io.File

private val suspiciousInstrumentationPackages = setOf(
    "de.robv.android.xposed.installer",
    "org.lsposed.manager",
    "org.meowcat.edxposed.manager",
    "com.saurik.substrate"
)

private val suspiciousInstrumentationClasses = setOf(
    "de.robv.android.xposed.XposedBridge",
    "de.robv.android.xposed.XposedHelpers",
    "com.saurik.substrate.MS"
)

private val trustedInstallerPackages = setOf(
    "com.android.vending",
    "com.amazon.venezia",
    "com.sec.android.app.samsungapps",
    "com.huawei.appmarket",
    "com.xiaomi.mipicks"
)

private val sideloadInstallerPackages = setOf(
    "com.android.packageinstaller",
    "com.google.android.packageinstaller",
    "com.samsung.android.packageinstaller",
    "com.miui.packageinstaller"
)

internal fun detectHardeningRoot(
    context: Context,
    findings: MutableList<String>
): Boolean {
    var detected = false

    listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su"
    ).forEach { path ->
        if (File(path).exists()) {
            findings += "Root: su binary found at $path"
            detected = true
        }
    }

    listOf(
        "com.topjohnwu.magisk",
        "com.noshufou.android.su",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser"
    ).forEach { pkg ->
        if (isHardeningPackageInstalled(context, pkg)) {
            findings += "Root: root app detected ($pkg)"
            detected = true
        }
    }

    val buildTags = Build.TAGS
    if (buildTags != null && buildTags.contains("test-keys")) {
        findings += "Root: test-keys build tag detected"
        detected = true
    }

    return detected
}

internal fun detectHardeningDebugger(
    findings: MutableList<String>
): Boolean {
    if (BuildConfig.DEBUG) return false

    if (android.os.Debug.isDebuggerConnected()) {
        findings += "Debugger: connected in release build"
        return true
    }

    return false
}

internal fun detectHardeningEmulator(
    findings: MutableList<String>
): Boolean {
    val indicators = listOf(
        Build.FINGERPRINT.startsWith("generic"),
        Build.FINGERPRINT.startsWith("unknown"),
        Build.MODEL.contains("google_sdk"),
        Build.MODEL.contains("Emulator"),
        Build.MODEL.contains("Android SDK built for x86"),
        Build.MANUFACTURER.contains("Genymotion"),
        Build.BRAND.startsWith("generic"),
        Build.DEVICE.startsWith("generic"),
        Build.HARDWARE == "goldfish",
        Build.HARDWARE == "ranchu",
        Build.PRODUCT == "sdk",
        Build.PRODUCT == "google_sdk",
        Build.PRODUCT == "sdk_x86",
        Build.PRODUCT == "vbox86p"
    )

    val detected = indicators.any { it }
    if (detected) {
        findings += "Emulator: device characteristics match emulator profile"
    }

    return detected
}

internal fun detectHardeningInstrumentation(
    context: Context,
    findings: MutableList<String>
): Boolean {
    val installedPackages = suspiciousInstrumentationPackages.filterTo(mutableSetOf()) { pkg ->
        isHardeningPackageInstalled(context, pkg)
    }

    val resolvableClasses = suspiciousInstrumentationClasses.filterTo(mutableSetOf()) { className ->
        canResolveHardeningClass(className)
    }

    return detectHardeningInstrumentationFromSignals(
        installedPackages = installedPackages,
        resolvableClasses = resolvableClasses,
        findings = findings
    )
}

internal fun detectHardeningInstrumentationFromSignals(
    installedPackages: Set<String>,
    resolvableClasses: Set<String>,
    findings: MutableList<String>
): Boolean {
    installedPackages.forEach { pkg ->
        findings += "Instrumentation: suspicious package detected ($pkg)"
    }

    resolvableClasses.forEach { className ->
        findings += "Instrumentation: suspicious class resolved ($className)"
    }

    return installedPackages.isNotEmpty() || resolvableClasses.isNotEmpty()
}

internal fun detectHardeningInstallerTrust(
    context: Context,
    findings: MutableList<String>
): Boolean {
    if (BuildConfig.DEBUG) return false

    val installerPackageName = resolveHardeningInstallerPackageName(context)
    return detectHardeningInstallerTrustFromSource(installerPackageName, findings)
}

internal fun detectHardeningInstallerTrustFromSource(
    installerPackageName: String?,
    findings: MutableList<String>
): Boolean {
    val normalizedInstaller = installerPackageName?.trim().orEmpty()

    if (normalizedInstaller.isBlank()) {
        findings += "Installer trust: installer source unavailable"
        return true
    }

    if (normalizedInstaller in trustedInstallerPackages) {
        return false
    }

    if (normalizedInstaller in sideloadInstallerPackages) {
        findings += "Installer trust: sideload-style installer detected ($normalizedInstaller)"
        return true
    }

    findings += "Installer trust: unrecognized installer source ($normalizedInstaller)"
    return true
}

internal fun detectHardeningTamperSignal(
    findings: MutableList<String>
): Boolean {
    if (BuildConfig.DEBUG) return false
    return SecurityTamperSignal.detect(findings)
}

private fun resolveHardeningInstallerPackageName(context: Context): String? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager
                .getInstallSourceInfo(context.packageName)
                .installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    } catch (_: Throwable) {
        null
    }
}

private fun canResolveHardeningClass(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (_: Throwable) {
        false
    }
}

private fun isHardeningPackageInstalled(
    context: Context,
    packageName: String
): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: Throwable) {
        false
    }
}
