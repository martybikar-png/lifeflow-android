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
            findings = listOf("Emulator: detected")
        )
        assertFalse(report.isCritical)
        assertTrue(report.isDegraded)
        assertFalse(report.isClean)
    }
}
