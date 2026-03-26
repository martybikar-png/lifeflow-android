package com.lifeflow.security.hardening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallerTrustHardeningTest {

    @Test
    fun `trusted installer stays clean`() {
        val findings = mutableListOf<String>()

        val detected = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = "com.android.vending",
            findings = findings
        )

        assertFalse(detected)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun `missing installer source becomes trust warning`() {
        val findings = mutableListOf<String>()

        val detected = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = null,
            findings = findings
        )

        assertTrue(detected)
        assertTrue(findings.any { it.contains("installer source unavailable", ignoreCase = true) })
    }

    @Test
    fun `sideload style installer becomes trust warning`() {
        val findings = mutableListOf<String>()

        val detected = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = "com.google.android.packageinstaller",
            findings = findings
        )

        assertTrue(detected)
        assertTrue(findings.any { it.contains("sideload-style installer", ignoreCase = true) })
    }

    @Test
    fun `unrecognized installer becomes trust warning`() {
        val findings = mutableListOf<String>()

        val detected = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = "example.unknown.installer",
            findings = findings
        )

        assertTrue(detected)
        assertTrue(findings.any { it.contains("unrecognized installer source", ignoreCase = true) })
    }
}
