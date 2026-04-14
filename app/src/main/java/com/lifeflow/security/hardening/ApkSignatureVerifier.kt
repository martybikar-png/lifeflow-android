package com.lifeflow.security.hardening

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.lifeflow.BuildConfig
import java.security.MessageDigest

/**
 * APK Signature Verification — detects repackaged/modified APKs.
 *
 * Compares runtime signature against expected release signature.
 * Fail-closed: mismatch or missing release expectation = CRITICAL finding.
 */
internal object ApkSignatureVerifier {

    private const val DEBUG_SIGNATURE_PLACEHOLDER = "DEBUG_SIGNATURE_PLACEHOLDER"

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

        if (BuildConfig.DEBUG) {
            return SignatureResult(
                isValid = true,
                actualSignature = actualSignature,
                findings = findings
            )
        }

        val expectedSignature = normalizeSha256(BuildConfig.EXPECTED_RELEASE_SIGNATURE_SHA256)

        if (expectedSignature == null || expectedSignature == DEBUG_SIGNATURE_PLACEHOLDER) {
            findings += "Signature: release signature expectation is not configured"
            return SignatureResult(
                isValid = false,
                actualSignature = actualSignature,
                findings = findings
            )
        }

        val isValid = actualSignature.equals(expectedSignature, ignoreCase = true)

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

    private fun normalizeSha256(raw: String?): String? {
        val normalized = raw
            ?.trim()
            ?.replace(":", "")
            ?.uppercase()
            ?: return null

        return if (
            normalized == DEBUG_SIGNATURE_PLACEHOLDER ||
            Regex("^[0-9A-F]{64}$").matches(normalized)
        ) {
            normalized
        } else {
            null
        }
    }
}
