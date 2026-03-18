package com.lifeflow.domain.security

import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary contract for observing current trust posture.
 */
interface TrustStatePort {
    fun currentTrustState(): TrustState
    fun observeTrustState(): Flow<TrustState>
}
