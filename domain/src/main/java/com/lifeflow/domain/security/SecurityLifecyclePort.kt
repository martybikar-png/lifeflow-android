package com.lifeflow.domain.security

/**
 * Domain boundary contract for compromise, lockout, and recovery flows.
 *
 * This port exposes the lifecycle side of security posture management without
 * leaking infrastructure-specific implementation details into domain logic.
 */
interface SecurityLifecyclePort {
    fun onCompromised(reason: CompromiseReason)
    fun onLockout(context: LockoutContext)
    fun recoveryOptions(): List<RecoveryOption>
}
