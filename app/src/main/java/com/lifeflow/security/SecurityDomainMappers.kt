package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.TrustState as DomainTrustState

internal fun TrustState.toDomainTrustState(): DomainTrustState =
    when (this) {
        TrustState.VERIFIED -> DomainTrustState.VERIFIED
        TrustState.DEGRADED -> DomainTrustState.DEGRADED
        TrustState.COMPROMISED -> DomainTrustState.COMPROMISED
    }

internal fun DomainTrustState.toAppTrustState(): TrustState =
    when (this) {
        DomainTrustState.VERIFIED -> TrustState.VERIFIED
        DomainTrustState.DEGRADED -> TrustState.DEGRADED
        DomainTrustState.COMPROMISED -> TrustState.COMPROMISED
    }

internal fun RuleAction.toDomainOperation(): DomainOperation =
    when (this) {
        RuleAction.READ_BY_ID -> DomainOperation.READ_IDENTITY_BY_ID
        RuleAction.READ_ACTIVE -> DomainOperation.READ_ACTIVE_IDENTITY
        RuleAction.WRITE_SAVE -> DomainOperation.SAVE_IDENTITY
        RuleAction.WRITE_DELETE -> DomainOperation.DELETE_IDENTITY
    }

internal fun DomainOperation.toRuleAction(): RuleAction =
    when (this) {
        DomainOperation.READ_IDENTITY_BY_ID -> RuleAction.READ_BY_ID
        DomainOperation.READ_ACTIVE_IDENTITY -> RuleAction.READ_ACTIVE
        DomainOperation.SAVE_IDENTITY -> RuleAction.WRITE_SAVE
        DomainOperation.DELETE_IDENTITY -> RuleAction.WRITE_DELETE
    }
