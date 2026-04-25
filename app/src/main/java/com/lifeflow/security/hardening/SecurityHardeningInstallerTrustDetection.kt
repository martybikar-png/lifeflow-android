package com.lifeflow.security.hardening

import android.content.Context
import android.os.Build
import com.lifeflow.BuildConfig

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
