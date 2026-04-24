package com.lifeflow

/**
 * Executes a controlled protected-runtime rebuild.
 *
 * Purpose:
 * - keep rebuild execution semantics out of LifeFlowAppRuntime branching
 * - reuse the same restart path for startup and future non-startup security recovery
 * - keep restart logging stable and explicit
 */
internal object ProtectedRuntimeRebuildController {

    fun execute(
        request: ProtectedRuntimeRebuildRequest,
        prepareRestartAndClearRuntime: (
            IntegrityRuntimeRestartReason,
            Int?
        ) -> Unit,
        restart: () -> Boolean,
        logInfo: (String) -> Unit,
        logWarning: (String) -> Unit
    ): Boolean {
        prepareRestartAndClearRuntime(
            request.reason,
            request.startupAutoRecoveryAttempt
        )

        request.logMessage?.let(logWarning)

        val restarted = restart()

        if (restarted) {
            logInfo(
                buildString {
                    append("Protected runtime rebuild re-entry started with reason=")
                    append(request.reason.name)
                    request.startupAutoRecoveryAttempt?.let {
                        append(", autoRecoveryAttempt=")
                        append(it)
                    }
                }
            )
        } else {
            logWarning(
                "Protected runtime rebuild re-entry failed; startup failure message preserved."
            )
        }

        return restarted
    }
}
