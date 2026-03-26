package com.lifeflow.security.hardening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityHardeningGuardTest {

    @Test
    fun `instrumentation signals detect suspicious package`() {
        val findings = mutableListOf<String>()
        val detected = SecurityHardeningGuard.detectInstrumentationFromSignals(
            installedPackages = setOf("org.lsposed.manager"),
            resolvableClasses = emptySet(),
            findings = findings
        )
        assertTrue(detected)
        assertTrue(findings.any { it.contains("suspicious package", ignoreCase = true) })
    }

    @Test
    fun `instrumentation signals detect suspicious class`() {
        val findings = mutableListOf<String>()
        val detected = SecurityHardeningGuard.detectInstrumentationFromSignals(
            installedPackages = emptySet(),
            resolvableClasses = setOf("de.robv.android.xposed.XposedBridge"),
            findings = findings
        )
        assertTrue(detected)
        assertTrue(findings.any { it.contains("suspicious class", ignoreCase = true) })
    }

    @Test
    fun `instrumentation signals stay clean when no signals are present`() {
        val findings = mutableListOf<String>()
        val detected = SecurityHardeningGuard.detectInstrumentationFromSignals(
            installedPackages = emptySet(),
            resolvableClasses = emptySet(),
            findings = findings
        )
        assertFalse(detected)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun `hardening report treats instrumentation as critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = true,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Instrumentation: suspicious package detected (org.lsposed.manager)")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isDegraded)
        assertFalse(report.isClean)
    }

    @Test
    fun `hardening report keeps emulator only as degraded`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = true,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Emulator: device characteristics match emulator profile")
        )
        assertFalse(report.isCritical)
        assertTrue(report.isDegraded)
        assertFalse(report.isClean)
    }
}
