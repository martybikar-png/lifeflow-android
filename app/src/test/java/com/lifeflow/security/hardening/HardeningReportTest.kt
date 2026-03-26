package com.lifeflow.security.hardening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HardeningReportTest {

    @Test
    fun `clean report has no critical or degraded flags`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = emptyList()
        )
        assertTrue(report.isClean)
        assertFalse(report.isCritical)
        assertFalse(report.isDegraded)
    }

    @Test
    fun `root detected is critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = true,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Root: su binary found")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isClean)
    }

    @Test
    fun `debugger detected is critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = true,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Debugger: connected")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isClean)
    }

    @Test
    fun `emulator detected is degraded not critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = true,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Emulator: detected")
        )
        assertFalse(report.isCritical)
        assertTrue(report.isDegraded)
        assertFalse(report.isClean)
    }

    @Test
    fun `installer trust warning is degraded not critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = true,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Installer trust: installer source unavailable")
        )
        assertFalse(report.isCritical)
        assertTrue(report.isDegraded)
        assertFalse(report.isClean)
    }

    @Test
    fun `tamper signal is critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = true,
            signatureInvalid = false,
            runtimeIntegrityFailed = false,
            findings = listOf("Tamper: suspicious runtime mapping detected (frida)")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isDegraded)
        assertFalse(report.isClean)
    }

    @Test
    fun `signature invalid is critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = true,
            runtimeIntegrityFailed = false,
            findings = listOf("Signature: APK signature mismatch")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isClean)
    }

    @Test
    fun `runtime integrity failed is critical`() {
        val report = SecurityHardeningGuard.HardeningReport(
            rootDetected = false,
            debuggerDetected = false,
            emulatorDetected = false,
            instrumentationDetected = false,
            installerTrustWarningDetected = false,
            tamperSignalDetected = false,
            signatureInvalid = false,
            runtimeIntegrityFailed = true,
            findings = listOf("Integrity: TracerPid detected")
        )
        assertTrue(report.isCritical)
        assertFalse(report.isClean)
    }
}
