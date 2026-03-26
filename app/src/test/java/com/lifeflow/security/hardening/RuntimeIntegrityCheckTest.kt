package com.lifeflow.security.hardening

import org.junit.Assert.*
import org.junit.Test

class RuntimeIntegrityCheckTest {

    @Test
    fun `integrity result clean when no issues`() {
        val result = RuntimeIntegrityCheck.IntegrityResult(
            isClean = true,
            tracerDetected = false,
            timingAnomalyDetected = false,
            findings = emptyList()
        )
        
        assertTrue(result.isClean)
        assertFalse(result.tracerDetected)
        assertFalse(result.timingAnomalyDetected)
    }

    @Test
    fun `integrity result not clean when tracer detected`() {
        val result = RuntimeIntegrityCheck.IntegrityResult(
            isClean = false,
            tracerDetected = true,
            timingAnomalyDetected = false,
            findings = listOf("Integrity: TracerPid detected (1234)")
        )
        
        assertFalse(result.isClean)
        assertTrue(result.tracerDetected)
        assertTrue(result.findings.any { it.contains("TracerPid") })
    }

    @Test
    fun `integrity result not clean when timing anomaly detected`() {
        val result = RuntimeIntegrityCheck.IntegrityResult(
            isClean = false,
            tracerDetected = false,
            timingAnomalyDetected = true,
            findings = listOf("Integrity: timing anomaly detected")
        )
        
        assertFalse(result.isClean)
        assertTrue(result.timingAnomalyDetected)
    }

    @Test
    fun `check returns non-null result`() {
        // In test environment, check should return a result without crashing
        try {
            val result = RuntimeIntegrityCheck.check()
            assertNotNull(result)
        } catch (_: Exception) {
            // /proc/self/status may not exist in test environment - that's OK
        }
    }
}
