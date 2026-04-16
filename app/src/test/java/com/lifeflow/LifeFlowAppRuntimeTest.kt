package com.lifeflow

import android.content.ContextWrapper
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeFlowAppRuntimeTest {

    @Test
    fun `runIntegrityTrustStartupCheckNow reports verified verdict on success`() {
        val reportedResponses = mutableListOf<IntegrityTrustVerdictResponse>()
        val runtime = newRuntime(reportedResponses)

        var capturedPayload: String? = null

        runtime.runIntegrityTrustStartupCheckNow { payload ->
            capturedPayload = payload
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "VERIFIED: SERVER_OK"
            )
        }

        assertEquals(fixedStartupContext().serializeIntegrityPayload(), capturedPayload)
        assertEquals(1, reportedResponses.size)
        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, reportedResponses.single().verdict)
        assertEquals("VERIFIED: SERVER_OK", reportedResponses.single().reason)
    }

    @Test
    fun `runIntegrityTrustStartupCheckNow degrades on failure`() {
        val reportedResponses = mutableListOf<IntegrityTrustVerdictResponse>()
        val runtime = newRuntime(reportedResponses)

        runtime.runIntegrityTrustStartupCheckNow {
            throw IllegalStateException("network down")
        }

        assertEquals(1, reportedResponses.size)
        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, reportedResponses.single().verdict)
        assertTrue(reportedResponses.single().reason.contains("STARTUP_VERDICT_FAILURE"))
        assertTrue(reportedResponses.single().reason.contains("IllegalStateException"))
    }

    @Test
    fun `tryScheduleIntegrityTrustStartupCheck schedules only once when configured`() {
        val runtime = newRuntime(mutableListOf())

        assertTrue(
            runtime.tryScheduleIntegrityTrustStartupCheck(isConfigured = true)
        )
        assertFalse(
            runtime.tryScheduleIntegrityTrustStartupCheck(isConfigured = true)
        )
    }

    @Test
    fun `tryScheduleIntegrityTrustStartupCheck does not schedule when not configured`() {
        val runtime = newRuntime(mutableListOf())

        assertFalse(
            runtime.tryScheduleIntegrityTrustStartupCheck(isConfigured = false)
        )
        assertTrue(
            runtime.tryScheduleIntegrityTrustStartupCheck(isConfigured = true)
        )
    }

    private fun newRuntime(
        reportedResponses: MutableList<IntegrityTrustVerdictResponse>
    ): LifeFlowAppRuntime {
        return LifeFlowAppRuntime(
            applicationContext = ContextWrapper(null),
            launchBackgroundTask = { _, block -> block() },
            reportIntegrityTrustVerdictResponse = { response ->
                reportedResponses += response
            },
            startupIntegrityContextFactory = { fixedStartupContext() }
        )
    }

    private fun fixedStartupContext(): IntegrityStartupRequestContext {
        return IntegrityStartupRequestContext(
            schemaVersion = 1,
            packageName = "com.lifeflow.test",
            versionName = "1.0-test",
            versionCode = 42,
            buildType = "debug",
            startupPhase = "application_startup",
            requestedAtEpochMs = 123456789L
        )
    }
}
