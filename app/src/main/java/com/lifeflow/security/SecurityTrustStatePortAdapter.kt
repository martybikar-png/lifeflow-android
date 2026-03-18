package com.lifeflow.security

import com.lifeflow.domain.security.TrustState
import com.lifeflow.domain.security.TrustStatePort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecurityTrustStatePortAdapter : TrustStatePort {

    override fun currentTrustState(): TrustState {
        return SecurityRuleEngine.getTrustState().toDomainTrustState()
    }

    override fun observeTrustState(): Flow<TrustState> {
        return SecurityRuleEngine.trustState.map { it.toDomainTrustState() }
    }
}
