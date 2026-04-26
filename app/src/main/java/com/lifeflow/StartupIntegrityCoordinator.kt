package com.lifeflow

import android.content.Context
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import kotlinx.coroutines.CancellationException

internal data class StartupIntegrityLifecycleSnapshot(
    val restartReason: IntegrityRuntimeRestartReason?,
    val autoRecoveryAttempt: Int?,
    val rebuildSource: ProtectedRuntimeRebuildSource?,
    val recoverySignal: String?
)

internal data class StartupIntegrityExecutionRequest(
    val trigger: IntegrityStartupCheckTrigger,
    val applicationContext: Context,
    val lifecycleSnapshot: StartupIntegrityLifecycleSnapshot,
    val startupProcessSequence: Int,
    val startupRequestId: String,
    val requestedAtEpochMs: Long
) {
    init {
        require(startupProcessSequence > 0) {
            "startupProcessSequence must be > 0."
        }
        require(startupRequestId.isNotBlank()) {
            "startupRequestId must not be blank."
        }
        require(requestedAtEpochMs > 0L) {
            "requestedAtEpochMs must be > 0."
        }
    }
}

internal data class StartupIntegrityExecutionResult(
    val infoLogMessage: String? = null,
    val warningLogMessage: String? = null,
    val warningThrowable: Throwable? = null,
    val recoveryDecision: IntegrityStartupRecoveryDecision? = null,
    val recoveryRebuildSource: ProtectedRuntimeRebuildSource? = null,
    val recoverySignal: String? = null
) {
    init {
        if (recoveryDecision?.action == IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD) {
            require(recoveryRebuildSource != null) {
                "recoveryRebuildSource is required for protected runtime rebuild requests."
            }
            require(!recoverySignal.isNullOrBlank()) {
                "recoverySignal is required for protected runtime rebuild requests."
            }
        }
    }
}

internal class StartupIntegrityCoordinator(
    private val startupIntegrityContextFactory: (Context) -> IntegrityStartupRequestContext,
    private val reportIntegrityTrustVerdictResponse: (IntegrityTrustVerdictResponse) -> Unit
) {

    suspend fun execute(
        request: StartupIntegrityExecutionRequest,
        requestServerVerdict: suspend (String) -> IntegrityTrustVerdictResponse
    ): StartupIntegrityExecutionResult {
        val requestContext = bindRequestContext(request)
        val payload = requestContext.serializeIntegrityPayload()

        return try {
            val response = requestServerVerdict(payload)
            reportIntegrityTrustVerdictResponse(response)

            val recoveryDecision = classifyIntegrityStartupRecoveryAction(
                trigger = request.trigger,
                response = response
            )

            when (recoveryDecision.action) {
                IntegrityStartupRecoveryAction.NONE -> {
                    StartupIntegrityExecutionResult(
                        infoLogMessage = buildString {
                            append("Startup integrity trust verdict applied for ")
                            append(request.trigger.name)
                            append(": ")
                            append(response.verdict)
                            append(" / ")
                            append(response.decision)
                            response.decisionReasonCode?.let {
                                append(" [")
                                append(it)
                                append(']')
                            }
                            append(" (")
                            append(response.reason)
                            append(')')
                        }
                    )
                }

                IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD -> {
                    StartupIntegrityExecutionResult(
                        recoveryDecision = recoveryDecision,
                        recoveryRebuildSource =
                            ProtectedRuntimeRebuildSource.STARTUP_INTEGRITY_POLICY,
                        recoverySignal = "STARTUP_INTEGRITY_POLICY_REBUILD"
                    )
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            reportIntegrityTrustVerdictResponse(
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason =
                        "STARTUP_VERDICT_FAILURE: ${exception::class.java.simpleName}: " +
                            exception.message
                )
            )

            val recoveryDecision = classifyIntegrityStartupFailureRecoveryAction(
                trigger = request.trigger,
                throwable = exception
            )

            when (recoveryDecision.action) {
                IntegrityStartupRecoveryAction.NONE -> {
                    StartupIntegrityExecutionResult(
                        warningLogMessage =
                            "Startup integrity trust verdict request failed for " +
                                "${request.trigger.name}.",
                        warningThrowable = exception
                    )
                }

                IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD -> {
                    StartupIntegrityExecutionResult(
                        recoveryDecision = recoveryDecision,
                        recoveryRebuildSource =
                            ProtectedRuntimeRebuildSource.STARTUP_INTEGRITY_REQUEST_FAILURE,
                        recoverySignal = "STARTUP_INTEGRITY_REQUEST_FAILURE_REBUILD"
                    )
                }
            }
        }
    }

    private fun bindRequestContext(
        request: StartupIntegrityExecutionRequest
    ): IntegrityStartupRequestContext {
        return startupIntegrityContextFactory(request.applicationContext).copy(
            startupTrigger = request.trigger.payloadValue,
            startupProcessSequence = request.startupProcessSequence,
            startupRequestId = request.startupRequestId,
            requestedAtEpochMs = request.requestedAtEpochMs,
            startupRestartReason =
                request.lifecycleSnapshot.restartReason?.payloadValue,
            startupAutoRecoveryAttempt =
                request.lifecycleSnapshot.autoRecoveryAttempt,
            startupRebuildSource =
                request.lifecycleSnapshot.rebuildSource?.payloadValue,
            startupRecoverySignal =
                request.lifecycleSnapshot.recoverySignal
        )
    }
}
