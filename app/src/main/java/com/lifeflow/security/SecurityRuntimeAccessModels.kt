package com.lifeflow.security

import com.lifeflow.domain.security.AuthorizationPolicy
import com.lifeflow.domain.security.DomainOperation

internal data class SecurityRuntimeAccessDecision(
    val allowed: Boolean,
    val effectiveTrustState: TrustState,
    val denialCode: SecurityRuntimeDecisionCode? = null
)

internal data class SecurityRuntimeAuthorizationRequest(
    val operation: DomainOperation,
    val authorizationPolicy: AuthorizationPolicy,
    val hasRecentAuthentication: Boolean
)

internal enum class SecurityRuntimeAuthorizationOutcome {
    ALLOWED,
    DENIED,
    REQUIRES_ELEVATION,
    LOCKED,
    REQUIRES_EMERGENCY_RESOLUTION
}

internal data class SecurityRuntimeAuthorizationDecision(
    val outcome: SecurityRuntimeAuthorizationOutcome,
    val effectiveTrustState: TrustState,
    val code: SecurityRuntimeDecisionCode? = null
)

internal enum class SecurityRuntimeOperationOutcome {
    ALLOWED,
    DENIED,
    LOCKED
}

internal data class SecurityRuntimeOperationDecision(
    val outcome: SecurityRuntimeOperationOutcome,
    val effectiveTrustState: TrustState,
    val code: SecurityRuntimeDecisionCode? = null
)