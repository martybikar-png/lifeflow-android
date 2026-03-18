package com.lifeflow.domain.security

/**
 * Domain boundary contract for operation authorization.
 *
 * This port answers a single explicit question:
 * may the requested domain operation proceed in the current auth context?
 */
interface SecurityAuthorizationPort {
    fun authorize(
        operation: DomainOperation,
        context: AuthContext
    ): AuthorizationResult
}
