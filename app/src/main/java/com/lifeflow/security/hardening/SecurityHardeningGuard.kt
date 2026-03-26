package com.lifeflow.security.hardening

import android.content.Context
import android.os.Build
import com.lifeflow.BuildConfig
import java.io.File

/**
 * SecurityHardeningGuard — protective shell around LifeFlow core.
 *
 * Checks: Root, Debugger, Emulator, Instrumentation/Hooking detection, Installer trust,
 * tamper signals, APK signature verification, and runtime integrity.
 *
 * Fail-closed: CRITICAL finding blocks vault initialization.
 * DEGRADED: emulator and installer trust warnings are surfaced but not blocked.
 */
object SecurityHardeningGuard {

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

    data class HardeningReport(
        val rootDetected: Boolean,
        val debuggerDetected: Boolean,
        val emulatorDetected: Boolean,
        val instrumentationDetected: Boolean,
        val installerTrustWarningDetected: Boolean,
        val tamperSignalDetected: Boolean,
        val signatureInvalid: Boolean,
        val runtimeIntegrityFailed: Boolean,
        val findings: List<String>
    ) {
        val isCritical: Boolean
            get() = rootDetected || debuggerDetected || instrumentationDetected || 
                    tamperSignalDetected || signatureInvalid || runtimeIntegrityFailed

        val isDegraded: Boolean
            get() = emulatorDetected || installerTrustWarningDetected

        val isClean: Boolean
            get() = !isCritical && !isDegraded
    }

    fun assess(context: Context): HardeningReport {
        val findings = mutableListOf<String>()
        
        // Original checks
        val rootDetected = detectRoot(context, findings)
        val debuggerDetected = detectDebugger(findings)
        val emulatorDetected = detectEmulator(findings)
        val instrumentationDetected = detectInstrumentation(context, findings)
        val installerTrustWarningDetected = detectInstallerTrust(context, findings)
        val tamperSignalDetected = detectTamperSignal(findings)
        
        // New checks (Phase 19-21)
        val signatureResult = ApkSignatureVerifier.verify(context)
        findings += signatureResult.findings
        
        val integrityResult = RuntimeIntegrityCheck.check()
        findings += integrityResult.findings

        return HardeningReport(
            rootDetected = rootDetected,
            debuggerDetected = debuggerDetected,
            emulatorDetected = emulatorDetected,
            instrumentationDetected = instrumentationDetected,
            installerTrustWarningDetected = installerTrustWarningDetected,
            tamperSignalDetected = tamperSignalDetected,
            signatureInvalid = !signatureResult.isValid,
            runtimeIntegrityFailed = !integrityResult.isClean,
            findings = findings
        )
    }

    /**
     * Quick integrity check for hot paths.
     * Returns true if immediate compromise is detected.
     */
    fun isCompromisedQuick(): Boolean {
        return RuntimeIntegrityCheck.isCompromised()
    }

    private fun detectRoot(context: Context, findings: MutableList<String>): Boolean {
        var detected = false

        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )

        suPaths.forEach { path ->
            if (File(path).exists()) {
                findings += "Root: su binary found at $path"
                detected = true
            }
        }

        val rootApps = listOf(
            "com.topjohnwu.magisk",
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser"
        )

        rootApps.forEach { pkg ->
            if (isPackageInstalled(context, pkg)) {
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

    private fun detectDebugger(findings: MutableList<String>): Boolean {
        if (BuildConfig.DEBUG) return false

        val debuggerAttached = android.os.Debug.isDebuggerConnected()
        if (debuggerAttached) {
            findings += "Debugger: connected in release build"
            return true
        }

        return false
    }

    private fun detectEmulator(findings: MutableList<String>): Boolean {
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

    private fun detectInstrumentation(context: Context, findings: MutableList<String>): Boolean {
        val installedPackages = suspiciousInstrumentationPackages.filterTo(mutableSetOf()) { pkg ->
            isPackageInstalled(context, pkg)
        }

        val resolvableClasses = suspiciousInstrumentationClasses.filterTo(mutableSetOf()) { className ->
            canResolveClass(className)
        }

        return detectInstrumentationFromSignals(
            installedPackages = installedPackages,
            resolvableClasses = resolvableClasses,
            findings = findings
        )
    }

    internal fun detectInstrumentationFromSignals(
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

    private fun detectInstallerTrust(context: Context, findings: MutableList<String>): Boolean {
        if (BuildConfig.DEBUG) return false

        val installerPackageName = resolveInstallerPackageName(context)
        return detectInstallerTrustFromSource(installerPackageName, findings)
    }

    internal fun detectInstallerTrustFromSource(
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

    private fun detectTamperSignal(findings: MutableList<String>): Boolean {
        if (BuildConfig.DEBUG) return false
        return SecurityTamperSignal.detect(findings)
    }

    private fun resolveInstallerPackageName(context: Context): String? {
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

    private fun canResolveClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
