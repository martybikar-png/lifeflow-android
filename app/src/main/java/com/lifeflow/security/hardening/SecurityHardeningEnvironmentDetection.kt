package com.lifeflow.security.hardening

import android.os.Build
import com.lifeflow.BuildConfig

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

internal fun detectHardeningTamperSignal(
    findings: MutableList<String>
): Boolean {
    if (BuildConfig.DEBUG) return false
    return SecurityTamperSignal.detect(findings)
}
