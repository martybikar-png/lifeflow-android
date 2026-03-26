package com.lifeflow.security.hardening

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * APK Signature Verification — detects repackaged/modified APKs.
 * 
 * Compares runtime signature against expected release signature.
 * Fail-closed: mismatch = CRITICAL finding.
 */
internal object ApkSignatureVerifier {

    // TODO: Replace with actual release signing certificate SHA-256 hash
    // Generate with: keytool -list -v -keystore release.keystore | grep SHA256
    private const val EXPECTED_RELEASE_SIGNATURE_SHA256 = "DEBUG_SIGNATURE_PLACEHOLDER"

    data class SignatureResult(
        val isValid: Boolean,
        val actualSignature: String?,
        val findings: List<String>
    )

    fun verify(context: Context): SignatureResult {
        val findings = mutableListOf<String>()
        
        val actualSignature = getApkSignatureSha256(context)
        
        if (actualSignature == null) {
            findings += "Signature: unable to retrieve APK signature"
            return SignatureResult(
                isValid = false,
                actualSignature = null,
                findings = findings
            )
        }

        // In debug builds, always pass (signature will differ)
        if (com.lifeflow.BuildConfig.DEBUG) {
            return SignatureResult(
                isValid = true,
                actualSignature = actualSignature,
                findings = findings
            )
        }

        // In release builds, verify against expected signature
        if (EXPECTED_RELEASE_SIGNATURE_SHA256 == "DEBUG_SIGNATURE_PLACEHOLDER") {
            // Not configured yet — pass but warn
            findings += "Signature: release signature not configured (development mode)"
            return SignatureResult(
                isValid = true,
                actualSignature = actualSignature,
                findings = findings
            )
        }

        val isValid = actualSignature.equals(EXPECTED_RELEASE_SIGNATURE_SHA256, ignoreCase = true)
        
        if (!isValid) {
            findings += "Signature: APK signature mismatch — possible repackaging detected"
        }

        return SignatureResult(
            isValid = isValid,
            actualSignature = actualSignature,
            findings = findings
        )
    }

    @Suppress("DEPRECATION")
    private fun getApkSignatureSha256(context: Context): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            signatures?.firstOrNull()?.let { signature ->
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(signature.toByteArray())
                hash.joinToString("") { "%02X".format(it) }
            }
        } catch (_: Throwable) {
            null
        }
    }
}
