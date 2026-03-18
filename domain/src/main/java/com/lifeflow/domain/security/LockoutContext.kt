package com.lifeflow.domain.security

/**
 * Explicit domain-level lockout context.
 *
 * Describes the state in which protected work is blocked and gives domain
 * flows enough context to react without depending on infrastructure details.
 */
data class LockoutContext(
    val attemptedOperation: DomainOperation? = null,
    val trustState: TrustState,
    val reason: LockReason
)
