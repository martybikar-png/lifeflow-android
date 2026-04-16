package com.lifeflow.domain.security

/**
 * Explicit authorization result at the domain boundary.
 *
 * Domain logic should react to this result directly instead of depending on
 * infrastructure exceptions for normal authorization flow.
 */
sealed interface AuthorizationResult {

    /**
     * Operation is allowed in the current security posture.
     */
    data object Allowed : AuthorizationResult

    /**
     * Operation is allowed only inside a temporary emergency window.
     */
    data class EmergencyAllowed(
        val window: EmergencyAccessWindow
    ) : AuthorizationResult

    /**
     * Operation is denied by policy in the current context.
     */
    data class Denied(
        val reason: DenialReason
    ) : AuthorizationResult

    /**
     * Operation may proceed only after stronger or fresher authorization.
     */
    data class RequiresElevation(
        val reason: ElevationReason
    ) : AuthorizationResult

    /**
     * Operation is blocked because the system is in a locked state.
     */
    data class Locked(
        val reason: LockReason
    ) : AuthorizationResult
}

/**
 * Non-terminal denial reasons.
 */
enum class DenialReason {
    OPERATION_NOT_ALLOWED,
    TRUST_NOT_SUFFICIENT,
    AUTH_CONTEXT_INVALID,
    POLICY_REJECTED,
    TRUSTED_BASE_ONLY_REQUIRED,
    EMERGENCY_NOT_APPROVED,
    EMERGENCY_WINDOW_EXPIRED
}

/**
 * Reasons why re-authorization or stronger authorization is required.
 */
enum class ElevationReason {
    RECENT_AUTH_REQUIRED,
    STRONGER_AUTH_REQUIRED,
    EMERGENCY_APPROVAL_REQUIRED
}

/**
 * Terminal / fail-closed lock reasons.
 */
enum class LockReason {
    COMPROMISED,
    LOCKED_OUT,
    RECOVERY_REQUIRED,
    EMERGENCY_REJECTED
}
