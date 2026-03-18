package com.lifeflow.domain.security

/**
 * Explicit domain-level operations that may require security authorization.
 *
 * This enum is the domain contract vocabulary.
 * It must stay independent from app/security implementation details.
 */
enum class DomainOperation {
    READ_IDENTITY_BY_ID,
    READ_ACTIVE_IDENTITY,
    SAVE_IDENTITY,
    DELETE_IDENTITY
}
