package com.lifeflow

import com.lifeflow.security.IntegrityTrustDecision
import com.lifeflow.security.IntegrityTrustVerdictResponse

/**
 * Recovery decisions for startup-scoped integrity flows.
 *
 * Purpose:
 * - keep rebuild/recovery semantics out of LifeFlowAppRuntime branching
 * - allow one controlled protected-runtime rebuild after a cold-start integrity failure
 * - prevent restart loops on SECURITY_RUNTIME_RESTART
 */
internal enum class IntegrityStartupRecoveryAction {
    NONE,
    REQUEST_PROTECTED_RUNTIME_REBUILD
}

internal data class IntegrityStartupRecoveryDecision(
    val action: IntegrityStartupRecoveryAction,
    val restartReason: IntegrityRuntimeRestartReason? = null,
    val logMessage: String? = null
)

internal fun classifyIntegrityStartupRecoveryAction(
    trigger: IntegrityStartupCheckTrigger,
    response: IntegrityTrustVerdictResponse
): IntegrityStartupRecoveryDecision {
    return when (trigger) {
        IntegrityStartupCheckTrigger.APPLICATION_COLD_START -> {
            when (response.decision) {
                IntegrityTrustDecision.DENY,
                IntegrityTrustDecision.LOCK -> {
                    IntegrityStartupRecoveryDecision(
                        action = IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD,
                        restartReason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
                        logMessage = buildString {
                            append("Protected runtime rebuild requested after cold-start zero-trust startup decision ")
                            append("(decision=")
                            append(response.decision)
                            append(", verdict=")
                            append(response.verdict)
                            response.decisionReasonCode?.let {
                                append(", code=")
                                append(it)
                            }
                            append(": ")
                            append(response.reason.take(220))
                            append(')')
                        }
                    )
                }

                IntegrityTrustDecision.ALLOW,
                IntegrityTrustDecision.STEP_UP,
                IntegrityTrustDecision.DEGRADED -> {
                    IntegrityStartupRecoveryDecision(
                        action = IntegrityStartupRecoveryAction.NONE
                    )
                }
            }
        }

        IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART -> {
            IntegrityStartupRecoveryDecision(
                action = IntegrityStartupRecoveryAction.NONE
            )
        }
    }
}

internal fun classifyIntegrityStartupFailureRecoveryAction(
    trigger: IntegrityStartupCheckTrigger,
    throwable: Throwable
): IntegrityStartupRecoveryDecision {
    return when (trigger) {
        IntegrityStartupCheckTrigger.APPLICATION_COLD_START -> {
            IntegrityStartupRecoveryDecision(
                action = IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD,
                restartReason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
                logMessage =
                    "Protected runtime rebuild requested after cold-start integrity failure " +
                        "(${throwable::class.java.simpleName}: ${(throwable.message ?: "unknown").take(220)})"
            )
        }

        IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART -> {
            IntegrityStartupRecoveryDecision(
                action = IntegrityStartupRecoveryAction.NONE
            )
        }
    }
}
