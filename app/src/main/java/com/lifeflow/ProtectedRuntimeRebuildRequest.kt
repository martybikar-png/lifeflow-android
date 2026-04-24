package com.lifeflow

/**
 * Structured request for a controlled protected-runtime rebuild.
 *
 * Purpose:
 * - keep restart reason, source and signal taxonomy explicit
 * - allow startup and future non-startup flows to use the same rebuild executor
 * - optionally carry startup auto-recovery attempt metadata
 */
internal data class ProtectedRuntimeRebuildRequest(
    val reason: IntegrityRuntimeRestartReason,
    val rebuildSource: ProtectedRuntimeRebuildSource =
        ProtectedRuntimeRebuildSource.INTERNAL,
    val recoverySignal: String? = null,
    val logMessage: String? = null,
    val startupAutoRecoveryAttempt: Int? = null
) {
    init {
        recoverySignal?.let {
            require(it.isNotBlank()) { "recoverySignal must not be blank when present." }
        }
        logMessage?.let {
            require(it.isNotBlank()) { "logMessage must not be blank when present." }
        }
        startupAutoRecoveryAttempt?.let {
            require(it > 0) { "startupAutoRecoveryAttempt must be > 0 when present." }
        }
    }
}
