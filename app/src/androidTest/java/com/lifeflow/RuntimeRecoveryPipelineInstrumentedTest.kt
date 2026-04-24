package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import com.lifeflow.security.TrustState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeRecoveryPipelineInstrumentedTest {

    @Test
    fun securityRestartLifecycle_and_startupCoordinator_bind_restart_metadata_into_payload() {
        val lifecycle = RuntimeIntegrityLifecycleState()

        lifecycle.preparePendingRestart(
            reason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            autoRecoveryAttempt = 2,
            rebuildSource = ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            recoverySignal = "PIPELINE_SIGNAL"
        )

        val startPlan = lifecycle.planStart(
            startupFailureMessage = "previous startup failure"
        )
        lifecycle.commitStart(startPlan)

        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val executionRequest = lifecycle.nextExecutionRequest(
            trigger = IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART,
            applicationContext = appContext
        )

        var capturedPayload: String? = null
        val reportedResponses = mutableListOf<IntegrityTrustVerdictResponse>()

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

        val response = IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "PIPELINE_OK"
        )

        runBlocking {
            coordinator.execute(
                request = executionRequest,
                requestServerVerdict = { payload ->
                    capturedPayload = payload
                    response
                }
            )
        }

        val payload = checkNotNull(capturedPayload)
        assertTrue(
            payload.contains(
                "startupTrigger=${IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART.payloadValue}"
            )
        )
        assertTrue(
            payload.contains(
                "startupRestartReason=${IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD.payloadValue}"
            )
        )
        assertTrue(payload.contains("startupAutoRecoveryAttempt=2"))
        assertTrue(
            payload.contains(
                "startupRebuildSource=${ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL.payloadValue}"
            )
        )
        assertTrue(payload.contains("startupRecoverySignal=PIPELINE_SIGNAL"))
        assertEquals(listOf(response), reportedResponses)
    }

    @Test
    fun bridge_and_runtimeRecoveryController_produce_rebuild_request_for_trust_compromise() {
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val result = bridge.onProtectedAccess(
            quickHardeningCompromised = false,
            trustCompromised = true
        )

        assertTrue(result)

        val request = checkNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.reason
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
        assertEquals(
            "TRUST_COMPROMISED_ON_PROTECTED_ACCESS",
            request.recoverySignal
        )
    }

    @Test
    fun surveillanceCoordinator_bridge_and_controller_chain_only_rebuild_when_runtime_is_ready() {
        var plannerCalls = 0
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            plannerCalls += 1
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val coordinator = RuntimeSecuritySurveillanceCoordinator(
            runtimeSecurityRecoveryBridge = bridge,
            quickHardeningCompromised = { true },
            currentTrustState = { TrustState.DEGRADED },
            logWarning = {}
        )

        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = false,
            hardeningReportAvailable = true
        )
        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = true,
            hardeningReportAvailable = false
        )

        assertEquals(0, plannerCalls)

        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = true,
            hardeningReportAvailable = true
        )

        assertEquals(1, plannerCalls)

        val request = checkNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.reason
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
        assertEquals(
            "QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS",
            request.recoverySignal
        )
    }
}
