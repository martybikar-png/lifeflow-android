package com.lifeflow

internal class ProtectedRuntimeRebuildExecutor(
    private val rebuildCoordinator: ProtectedRuntimeRebuildCoordinator,
    private val prepareRestartAndTeardown: (
        ProtectedRuntimeRebuildRequest,
        IntegrityRuntimeRestartReason,
        Int?
    ) -> Unit,
    private val restart: () -> Boolean,
    private val logInfo: (String) -> Unit,
    private val logWarning: (String) -> Unit
) {
    fun execute(
        request: ProtectedRuntimeRebuildRequest
    ): Boolean {
        val admission = rebuildCoordinator.tryAcquire(request)
        if (!admission.allowed) {
            admission.skipMessage?.let(logWarning)
            return false
        }

        return try {
            ProtectedRuntimeRebuildController.execute(
                request = request,
                prepareRestartAndClearRuntime = { reason, startupAutoRecoveryAttempt ->
                    prepareRestartAndTeardown(
                        request,
                        reason,
                        startupAutoRecoveryAttempt
                    )
                },
                restart = restart,
                logInfo = logInfo,
                logWarning = logWarning
            )
        } finally {
            rebuildCoordinator.release()
        }
    }
}
