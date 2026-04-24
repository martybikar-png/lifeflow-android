package com.lifeflow.security.hardening

import android.os.Debug
import android.os.SystemClock
import java.io.File

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

    private fun checkTracerPid(findings: MutableList<String>): Boolean {
        val tracerPid = readTracerPid()

        return if (tracerPid != 0) {
            findings += "Integrity: TracerPid detected ($tracerPid) — possible debugger/ptrace"
            true
        } else {
            false
        }
    }

    private fun checkTimingAnomaly(findings: MutableList<String>): Boolean {
        if (com.lifeflow.BuildConfig.DEBUG) return false

        val start = SystemClock.elapsedRealtimeNanos()

        @Suppress("UNUSED_VARIABLE")
        var dummy = 0
        for (i in 0 until 1000) {
            dummy = dummy xor i
        }

        val elapsed = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000

        if (elapsed > TIMING_THRESHOLD_MS) {
            findings += "Integrity: timing anomaly detected (${elapsed}ms > ${TIMING_THRESHOLD_MS}ms threshold)"
            return true
        }

        return false
    }

    fun isCompromised(): Boolean {
        if (Debug.isDebuggerConnected()) return true
        if (Debug.waitingForDebugger()) return true
        if (readTracerPid() != 0) return true

        return SecurityTamperSignal.snapshot().detected
    }

    private fun readTracerPid(): Int {
        return try {
            val status = File("/proc/self/status").readText()
            val tracerLine = status.lines().find { it.startsWith("TracerPid:") }
            tracerLine?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
        } catch (_: Throwable) {
            0
        }
    }
}
