package com.lifeflow

import android.os.Build

internal fun shouldBypassBiometricAuthForDebugEmulator(): Boolean {
    if (!BuildConfig.DEBUG) {
        return false
    }

    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    val device = Build.DEVICE.lowercase()
    val product = Build.PRODUCT.lowercase()
    val hardware = Build.HARDWARE.lowercase()

    return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("sdk") ||
            model.contains("emulator") ||
            manufacturer.contains("google") && product.contains("sdk") ||
            brand.contains("google") && device.contains("emu") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu")
}
