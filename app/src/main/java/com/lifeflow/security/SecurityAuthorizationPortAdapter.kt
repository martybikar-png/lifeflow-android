package com.lifeflow.security

import com.lifeflow.domain.security.AuthContext
import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DenialReason
import com.lifeflow.domain.security.ElevationReason
import com.lifeflow.domain.security.LockReason
import com.lifeflow.domain.security.SecurityAuthorizationPort

class SecurityAuthorizationPortAdapter : SecurityAuthorizationPort {

    override fun authorize(
        operation: com.lifeflow.domain.security.DomainOperation,
        context: AuthContext
    ): AuthorizationResult {
        val trustState = SecurityRuleEngine.getTrustState()

        if (trustState == TrustState.COMPROMISED) {
            return AuthorizationResult.Locked(LockReason.COMPROMISED)
        }

        val action = operation.toRuleAction()
        val sessionAuthorized = context.hasRecentAuthentication && SecurityAccessSession.isAuthorized()

        if (!sessionAuthorized) {
            return AuthorizationResult.RequiresElevation(
                ElevationReason.RECENT_AUTH_REQUIRED
            )
        }

        if (context.requiresStrictAuth && trustState != TrustState.VERIFIED) {
            return AuthorizationResult.RequiresElevation(
                ElevationReason.STRONGER_AUTH_REQUIRED
            )
        }

        return when (trustState) {
            TrustState.VERIFIED -> AuthorizationResult.Allowed

            TrustState.DEGRADED -> {
                if (action.isRead) {
                    AuthorizationResult.Allowed
                } else {
                    AuthorizationResult.Denied(
                        DenialReason.TRUST_NOT_SUFFICIENT
                    )
                }
            }

            TrustState.COMPROMISED -> AuthorizationResult.Locked(LockReason.COMPROMISED)
        }
    }
}
