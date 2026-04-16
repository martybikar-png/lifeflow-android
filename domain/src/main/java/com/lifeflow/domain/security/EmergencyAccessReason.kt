package com.lifeflow.domain.security

/**
 * Explicit audited reasons for requesting break-glass access.
 *
 * Keep this enum intentionally narrow.
 * Every value must correspond to a real reviewed emergency scenario.
 */
enum class EmergencyAccessReason {
    LOCKED_OUT_RECOVERY,
    CRITICAL_HEALTH_ACCESS,
    VAULT_RECOVERY_READONLY,
    MANUAL_BREAK_GLASS_APPROVED
}
