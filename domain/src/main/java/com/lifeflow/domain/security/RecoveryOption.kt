package com.lifeflow.domain.security

/**
 * Explicit recovery actions exposed at the domain boundary.
 *
 * These options describe what kind of recovery path is available without
 * exposing infrastructure-specific implementation details.
 */
enum class RecoveryOption {
    RETRY_AUTHENTICATION,
    RESTART_SECURE_SESSION,
    ENTER_BREAK_GLASS,
    EXIT_BREAK_GLASS,
    RESET_VAULT,
    CONTACT_SUPPORT
}
