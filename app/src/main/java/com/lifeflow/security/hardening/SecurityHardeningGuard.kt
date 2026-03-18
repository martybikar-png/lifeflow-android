package com.lifeflow.security.hardening

import android.content.Context
import android.os.Build
import com.lifeflow.BuildConfig
import java.io.File

/**
 * SecurityHardeningGuard — protective shell around LifeFlow core.
 *
 * Checks: Root, Debugger, Emulator detection.
 * Fail-closed: CRITICAL finding blocks vault initialization.
 * DEGRADED: emulator is warned but not blocked.
 */
object SecurityHardeningGuard {

    data class HardeningReport(
        val rootDetected: Boolean,
        val debuggerDetected: Boolean,
        val emulatorDetected: Boolean,
        val findings: List<String>
    ) {
        val isCritical: Boolean get() = rootDetected || debuggerDetected
        val isDegraded: Boolean get() = emulatorDetected
        val isClean: Boolean get() = !isCritical && !isDegraded
    }

    fun assess(context: Context): HardeningReport {
        val findings = mutableListOf<String>()
        val rootDetected = detectRoot(context, findings)
        val debuggerDetected = detectDebugger(findings)
        val emulatorDetected = detectEmulator(findings)
        return HardeningReport(
            rootDetected = rootDetected,
            debuggerDetected = debuggerDetected,
            emulatorDetected = emulatorDetected,
            findings = findings
        )
    }

    private fun detectRoot(context: Context, findings: MutableList<String>): Boolean {
        var detected = false

        val suPaths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su"
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
        if (detected) findings += "Emulator: device characteristics match emulator profile"
        return detected
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
