package com.lifeflow.security

import com.lifeflow.domain.security.AuthContext
import com.lifeflow.domain.security.AuthorizationPolicy

internal object SecurityAuthorizationContexts {

    fun standardProtectedCurrentSession(): AuthContext {
        return AuthContext(
            hasRecentAuthentication = SecurityAccessSession.isAuthorized(),
            authorizationPolicy = AuthorizationPolicy.STANDARD_PROTECTED
        )
    }

    fun strictProtectedCurrentSession(): AuthContext {
        return AuthContext(
            hasRecentAuthentication = SecurityAccessSession.isAuthorized(),
            authorizationPolicy = AuthorizationPolicy.STRICT_PROTECTED
        )
    }

    fun trustedBaseReadOnlyCurrentSession(): AuthContext {
        return AuthContext(
            hasRecentAuthentication = SecurityAccessSession.isAuthorized(),
            authorizationPolicy = AuthorizationPolicy.TRUSTED_BASE_READ_ONLY,
            emergencyRequest = SecurityEmergencyWindowStatePortAdapter
                .activeTrustedBaseReadOnlyRequestOrNull()
        )
    }
}
