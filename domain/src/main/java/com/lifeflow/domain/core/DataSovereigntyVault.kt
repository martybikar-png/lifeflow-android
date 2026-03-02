package com.lifeflow.domain.core

/**
 * DataSovereigntyVault is the single authority for:
 * - vault initialization state
 * - key/material readiness checks (via implementations)
 *
 * Domain-only interface (no Android dependencies).
 */
interface DataSovereigntyVault {
    fun isInitialized(): Boolean
    fun ensureInitialized()
}