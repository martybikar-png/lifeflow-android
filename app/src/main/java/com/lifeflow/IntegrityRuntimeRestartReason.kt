package com.lifeflow

/**
 * Explicit reasons for a security-relevant runtime restart.
 *
 * Purpose:
 * - make SECURITY_RUNTIME_RESTART semantically explicit
 * - avoid inferring restart meaning only from "second start in process"
 * - allow future recovery paths to request a restart with a concrete reason
 */
internal enum class IntegrityRuntimeRestartReason(
    val payloadValue: String
) {
    RUNTIME_CLOSE_AFTER_SUCCESSFUL_START("runtime_close_after_successful_start"),
    SECURITY_RECOVERY_REENTRY("security_recovery_reentry"),
    PROTECTED_RUNTIME_REBUILD("protected_runtime_rebuild")
}
