package com.lifeflow.security

import com.lifeflow.domain.security.TrustState as DomainTrustState

internal fun TrustState.toDomainTrustState(): DomainTrustState =
    when (this) {
        TrustState.VERIFIED -> DomainTrustState.VERIFIED
        TrustState.DEGRADED -> DomainTrustState.DEGRADED
        TrustState.EMERGENCY_LIMITED -> DomainTrustState.EMERGENCY_LIMITED
        TrustState.COMPROMISED -> DomainTrustState.COMPROMISED
    }

internal fun DomainTrustState.toAppTrustState(): TrustState =
    when (this) {
        DomainTrustState.VERIFIED -> TrustState.VERIFIED
        DomainTrustState.DEGRADED -> TrustState.DEGRADED
        DomainTrustState.EMERGENCY_LIMITED -> TrustState.EMERGENCY_LIMITED
        DomainTrustState.COMPROMISED -> TrustState.COMPROMISED
    }
