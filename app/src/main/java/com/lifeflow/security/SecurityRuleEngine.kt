package com.lifeflow.security

/**
 * Phase III — Rule Engine (V1)
 * Minimal enterprise-grade baseline:
 * - Deny by default
 * - Allow only if there is an active biometric session window
 *
 * In later phases, this is where we add:
 * - policy contexts
 * - trust states (Verified/Degraded/Compromised)
 * - rate limits / anti-replay
 * - audit hooks
 */
object SecurityRuleEngine {

    fun isAllowed(action: RuleAction): Boolean {
        // V1 policy:
        // Any identity crypto/storage operation requires active session.
        // (Later: allow some reads in Degraded mode, etc.)
        return SecurityAccessSession.isAuthorized()
    }

    fun requireAllowed(action: RuleAction, reason: String) {
        if (!isAllowed(action)) {
            throw SecurityException("RuleEngine denied: $action → $reason")
        }
    }
}