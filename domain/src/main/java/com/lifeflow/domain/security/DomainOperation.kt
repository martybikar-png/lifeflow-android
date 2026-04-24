package com.lifeflow.domain.security

/**
 * Explicit domain-level operations that may require security authorization.
 *
 * This enum is the domain contract vocabulary.
 * It must stay independent from app/security implementation details.
 */
enum class DomainOperationSecurityClass(
    val requiresSensitiveOperationsCapability: Boolean,
    val allowsTrustedBaseReadOnly: Boolean
) {
    READ_SENSITIVE(
        requiresSensitiveOperationsCapability = false,
        allowsTrustedBaseReadOnly = true
    ),
    WRITE_SENSITIVE(
        requiresSensitiveOperationsCapability = true,
        allowsTrustedBaseReadOnly = false
    )
}

enum class DomainOperation(
    val securityClass: DomainOperationSecurityClass
) {
    READ_IDENTITY_BY_ID(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    READ_ACTIVE_IDENTITY(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    SAVE_IDENTITY(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    ),
    DELETE_IDENTITY(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    ),

    READ_TWIN_SNAPSHOT(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    READ_WELLBEING_SNAPSHOT(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),

    READ_DIARY(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    WRITE_DIARY(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    ),

    READ_MEMORY(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    WRITE_MEMORY(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    ),

    READ_CONNECTION(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    WRITE_CONNECTION(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    ),

    READ_SHOPPING(
        securityClass = DomainOperationSecurityClass.READ_SENSITIVE
    ),
    WRITE_SHOPPING(
        securityClass = DomainOperationSecurityClass.WRITE_SENSITIVE
    )
}
