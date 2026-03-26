package com.lifeflow.security

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

/**
 * DeviceFingerprint — generates a stable device identifier for session binding.
 * 
 * Session is bound to device — stolen session tokens won't work on different device.
 * 
 * Uses non-sensitive device attributes (no IMEI, phone number, etc.)
 */
object DeviceFingerprint {

    /**
     * Generates SHA-256 hash of device attributes.
     * Stable across app reinstalls, changes only with factory reset.
     */
    @SuppressLint("HardwareIds")
    fun generate(context: Context): String {
        val components = buildString {
            // Android ID - unique per app signing key + device
            append(Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "")
            append("|")
            // Device hardware characteristics
            append(Build.BOARD)
            append("|")
            append(Build.BRAND)
            append("|")
            append(Build.DEVICE)
            append("|")
            append(Build.HARDWARE)
            append("|")
            append(Build.MANUFACTURER)
            append("|")
            append(Build.MODEL)
            append("|")
            append(Build.PRODUCT)
            append("|")
            // Build fingerprint
            append(Build.FINGERPRINT)
        }

        return sha256(components)
    }

    /**
     * Validates that current device matches stored fingerprint.
     */
    fun validate(context: Context, storedFingerprint: String): Boolean {
        val currentFingerprint = generate(context)
        return currentFingerprint == storedFingerprint
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
