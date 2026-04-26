package com.lifeflow.security

import com.lifeflow.domain.security.AuthContext
import com.lifeflow.domain.security.DomainOperation

internal object SecurityAuthorizationGate {

    fun lockedReasonOrNull(
        operation: DomainOperation,
        context: AuthContext,
        detail: String
    ): String? {
        return SecurityAuthorizationPortAdapter()
            .authorize(
                operation = operation,
                context = context
            )
            .toLockedReasonOrNull(detail)
    }

    fun standardProtectedLockedReasonOrNull(
        operation: DomainOperation,
        detail: String
    ): String? {
        return lockedReasonOrNull(
            operation = operation,
            context = SecurityAuthorizationContexts.standardProtectedCurrentSession(),
            detail = detail
        )
    }

    fun strictProtectedLockedReasonOrNull(
        operation: DomainOperation,
        detail: String
    ): String? {
        return lockedReasonOrNull(
            operation = operation,
            context = SecurityAuthorizationContexts.strictProtectedCurrentSession(),
            detail = detail
        )
    }

    fun trustedBaseReadOnlyLockedReasonOrNull(
        operation: DomainOperation,
        detail: String
    ): String? {
        return lockedReasonOrNull(
            operation = operation,
            context = SecurityAuthorizationContexts.trustedBaseReadOnlyCurrentSession(),
            detail = detail
        )
    }

    fun biometricBootstrapLockedReasonOrNull(
        operation: DomainOperation,
        detail: String
    ): String? {
        return SecurityRuntimeAccessPolicy
            .decideBiometricBootstrapOperation(operation)
            .toLockedReason(detail)
    }
}
