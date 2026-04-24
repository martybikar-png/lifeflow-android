package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupIntegrityCoordinatorInstrumentedTest {

    @Test
    fun securityRestartRequest_bindsLifecycleFieldsIntoPayload_andReportsVerifiedVerdict() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val reportedResponses = mutableListOf<IntegrityTrustVerdictResponse>()
        var capturedPayload: String? = null

        val coordinator = StartupIntegrityCoordinator(
            startupIntegrityContextFactory = { context ->
                IntegrityStartupRequestContext(
                    schemaVersion = 1,
                    packageName = context.packageName,
                    versionName = "1.0-test",
                    versionCode = 1,
                    buildType = "debug",
                    startupPhase = "application_startup",
                    startupTrigger = "placeholder",
                    startupProcessSequence = 1,
                    startupRequestId = "placeholder",
                    requestedAtEpochMs = 1L
                )
            },
            reportIntegrityTrustVerdictResponse = { response ->
                reportedResponses += response
            }
        )

        val request = StartupIntegrityExecutionRequest(
            trigger = IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART,
            applicationContext = appContext,
            lifecycleSnapshot = StartupIntegrityLifecycleSnapshot(
                restartReason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
                autoRecoveryAttempt = 3,
                rebuildSource = ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
                recoverySignal = "SIGNAL_X"
            ),
            startupProcessSequence = 7,
            startupRequestId = "req-7",
            requestedAtEpochMs = 123456789L
        )

        val response = IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "SERVER_OK"
        )

        val result = runBlocking {
            coordinator.execute(
                request = request,
                requestServerVerdict = { payload ->
                    capturedPayload = payload
                    response
                }
            )
        }

        assertEquals(listOf(response), reportedResponses)
        assertNull(result.recoveryDecision)
        assertNull(result.warningLogMessage)

        val payload = requireNotNull(capturedPayload)
        assertTrue(
            payload.contains(
                "startupTrigger=${IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART.payloadValue}"
            )
        )
        assertTrue(payload.contains("startupProcessSequence=7"))
        assertTrue(payload.contains("startupRequestId=req-7"))
        assertTrue(payload.contains("requestedAtEpochMs=123456789"))
        assertTrue(
            payload.contains(
                "startupRestartReason=${IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD.payloadValue}"
            )
        )
        assertTrue(payload.contains("startupAutoRecoveryAttempt=3"))
        assertTrue(
            payload.contains(
                "startupRebuildSource=${ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL.payloadValue}"
            )
        )
        assertTrue(payload.contains("startupRecoverySignal=SIGNAL_X"))
    }

    @Test
    fun coldStartVerified_returnsInfoMessage_withoutRecoveryRequest() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val coordinator = StartupIntegrityCoordinator(
            startupIntegrityContextFactory = { context ->
                IntegrityStartupRequestContext(
                    schemaVersion = 1,
                    packageName = context.packageName,
                    versionName = "1.0-test",
                    versionCode = 1,
                    buildType = "debug",
                    startupPhase = "application_startup",
                    startupTrigger = "placeholder",
                    startupProcessSequence = 1,
                    startupRequestId = "placeholder",
                    requestedAtEpochMs = 1L
                )
            },
            reportIntegrityTrustVerdictResponse = {}
        )

        val request = StartupIntegrityExecutionRequest(
            trigger = IntegrityStartupCheckTrigger.APPLICATION_COLD_START,
            applicationContext = appContext,
            lifecycleSnapshot = StartupIntegrityLifecycleSnapshot(
                restartReason = null,
                autoRecoveryAttempt = null,
                rebuildSource = null,
                recoverySignal = null
            ),
            startupProcessSequence = 1,
            startupRequestId = "cold-start-1",
            requestedAtEpochMs = 42L
        )

        val result = runBlocking {
            coordinator.execute(
                request = request,
                requestServerVerdict = {
                    IntegrityTrustVerdictResponse(
                        verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                        reason = "COLD_START_OK"
                    )
                }
            )
        }

        assertNull(result.recoveryDecision)
        assertTrue(
            requireNotNull(result.infoLogMessage)
                .contains("APPLICATION_COLD_START")
        )
    }
}
