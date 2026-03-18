package com.lifeflow.security

/**
 * Phase III (V1) enforcement actions.
 *
 * Deny-by-default:
 * any action not explicitly allowed by the rule engine must fail.
 */
enum class RuleAction {

    /**
     * Read a specific identity by id.
     */
    READ_BY_ID,

    /**
     * Read the currently active identity.
     */
    READ_ACTIVE,

    /**
     * Persist or update protected identity data.
     */
    WRITE_SAVE,

    /**
     * Delete protected identity data.
     */
    WRITE_DELETE;

    val isRead: Boolean
        get() = this == READ_BY_ID || this == READ_ACTIVE

    val isWrite: Boolean
        get() = this == WRITE_SAVE || this == WRITE_DELETE
}