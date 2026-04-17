package com.lifeflow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LifeFlowAppRuntimeInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun tryScheduleIntegrityTrustStartupCheck_returnsFalse_whenRuntimeIsNotConfigured() {
        val runtime = newTestRuntime()

        val scheduled = runtime.tryScheduleIntegrityTrustStartupCheck(
            isConfigured = false
        )

        assertFalse(scheduled)
    }

    @Test
    fun tryScheduleIntegrityTrustStartupCheck_returnsTrueOnlyOnce_whenRuntimeIsConfigured() {
        val runtime = newTestRuntime()

        val first = runtime.tryScheduleIntegrityTrustStartupCheck(
            isConfigured = true
        )
        val second = runtime.tryScheduleIntegrityTrustStartupCheck(
            isConfigured = true
        )

        assertTrue(first)
        assertFalse(second)
    }

    @Test
    fun runIntegrityTrustStartupCheckNow_reportsServerVerdictResponse_onSuccess() {
        var capturedResponse: IntegrityTrustVerdictResponse? = null

        val runtime = newTestRuntime(
            reportVerdict = { response ->
                capturedResponse = response
            }
        )

        runtime.runIntegrityTrustStartupCheckNow(
            requestServerVerdict = {
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                    reason = "VERIFIED: startup integrity ok"
                )
            }
        )

        val response = capturedResponse
        assertNotNull(response)
        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, response?.verdict)
        assertEquals("VERIFIED: startup integrity ok", response?.reason)
    }

    @Test
    fun runIntegrityTrustStartupCheckNow_reportsDegradedFailsafe_onFailure() {
        var capturedResponse: IntegrityTrustVerdictResponse? = null

        val runtime = newTestRuntime(
            reportVerdict = { response ->
                capturedResponse = response
            }
        )

        runtime.runIntegrityTrustStartupCheckNow(
            requestServerVerdict = {
                throw IllegalStateException("server unavailable")
            }
        )

        val response = capturedResponse
        assertNotNull(response)
        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, response?.verdict)
        assertTrue(
            response?.reason?.startsWith("STARTUP_VERDICT_FAILURE: IllegalStateException: server unavailable") == true
        )
    }

    private fun newTestRuntime(
        reportVerdict: (IntegrityTrustVerdictResponse) -> Unit = {}
    ): LifeFlowAppRuntime {
        return LifeFlowAppRuntime(
            applicationContext = context,
            runtimeBindingsFactory = { _, _ ->
                error("runtimeBindingsFactory is not used in this test")
            },
            launchBackgroundTask = { _, _ ->
                error("launchBackgroundTask is not used in this test")
            },
            reportIntegrityTrustVerdictResponse = reportVerdict
        )
    }
}
