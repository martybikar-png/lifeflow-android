package com.lifeflow

/**
 * Explicit trigger kinds for startup-scoped integrity checks.
 *
 * Purpose:
 * - avoid one hidden "already scheduled for this process" boolean
 * - make protected request intent explicit in the bound payload
 * - allow future security-relevant restart checks without pretending they are the same event
 */
internal enum class IntegrityStartupCheckTrigger(
    val payloadValue: String
) {
    APPLICATION_COLD_START("application_cold_start"),
    SECURITY_RUNTIME_RESTART("security_runtime_restart")
}
