package com.lifeflow.security

/**
 * Phase III (V1) enforcement actions.
 * Denied-by-default: any action not explicitly allowed must fail.
 */
enum class RuleAction {
    READ_BY_ID,
    READ_ACTIVE,
    WRITE_SAVE,
    WRITE_DELETE
}