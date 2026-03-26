package com.lifeflow.security.hardening

import android.os.Debug
import android.os.SystemClock

/**
 * RuntimeIntegrityCheck — detects runtime tampering attempts.
 * 
 * Checks for:
 * - Debugger attachment timing anomalies
 * - Breakpoint detection via timing
 * - TracerPid in /proc/self/status
 */
object RuntimeIntegrityCheck {

    private const val TIMING_THRESHOLD_MS = 100L

    data class IntegrityResult(
        val isClean: Boolean,
        val tracerDetected: Boolean,
        val timingAnomalyDetected: Boolean,
        val findings: List<String>
    )

    fun check(): IntegrityResult {
        val findings = mutableListOf<String>()
        
        val tracerDetected = checkTracerPid(findings)
        val timingAnomalyDetected = checkTimingAnomaly(findings)
        
        return IntegrityResult(
            isClean = !tracerDetected && !timingAnomalyDetected,
            tracerDetected = tracerDetected,
            timingAnomalyDetected = timingAnomalyDetected,
            findings = findings
        )
    }

    /**
     * Checks /proc/self/status for non-zero TracerPid.
     * Non-zero indicates ptrace attachment (debugger/strace).
     */
    private fun checkTracerPid(findings: MutableList<String>): Boolean {
        return try {
            val status = java.io.File("/proc/self/status").readText()
            val tracerLine = status.lines().find { it.startsWith("TracerPid:") }
            val tracerPid = tracerLine?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
            
            if (tracerPid != 0) {
                findings += "Integrity: TracerPid detected ($tracerPid) — possible debugger/ptrace"
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Timing-based debugger detection.
     * Breakpoints cause measurable delays in tight loops.
     */
    private fun checkTimingAnomaly(findings: MutableList<String>): Boolean {
        // Skip in debug builds
        if (com.lifeflow.BuildConfig.DEBUG) return false
        
        val start = SystemClock.elapsedRealtimeNanos()
        
        // Simple computation that should be instant
        @Suppress("UNUSED_VARIABLE")
        var dummy = 0
        for (i in 0 until 1000) {
            dummy = dummy xor i
        }
        
        val elapsed = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000 // to ms
        
        if (elapsed > TIMING_THRESHOLD_MS) {
            findings += "Integrity: timing anomaly detected (${elapsed}ms > ${TIMING_THRESHOLD_MS}ms threshold)"
            return true
        }
        
        return false
    }

    /**
     * Quick check suitable for hot paths.
     * Returns true if system appears compromised.
     */
    fun isCompromised(): Boolean {
        if (Debug.isDebuggerConnected()) return true
        
        return try {
            val status = java.io.File("/proc/self/status").readText()
            val tracerLine = status.lines().find { it.startsWith("TracerPid:") }
            val tracerPid = tracerLine?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
            tracerPid != 0
        } catch (_: Throwable) {
            false
        }
    }
}
