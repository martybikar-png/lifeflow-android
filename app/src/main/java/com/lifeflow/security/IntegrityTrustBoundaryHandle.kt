package com.lifeflow.security

/**
 * Narrow lifecycle handle for the integrity trust boundary.
 *
 * Purpose:
 * - keep Application unaware of integrity runtime implementation details
 * - expose only boundary lifecycle shutdown control at this stage
 */
internal interface IntegrityTrustBoundaryHandle : AutoCloseable {
    override fun close()
}
