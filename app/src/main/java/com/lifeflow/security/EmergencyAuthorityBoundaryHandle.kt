package com.lifeflow.security

/**
 * Narrow lifecycle handle for the emergency authority boundary.
 *
 * Purpose:
 * - keep Application unaware of runtime implementation details
 * - expose only boundary lifecycle shutdown control
 */
internal interface EmergencyAuthorityBoundaryHandle : AutoCloseable {
    override fun close()
}
