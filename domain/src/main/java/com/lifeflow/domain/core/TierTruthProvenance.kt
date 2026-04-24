package com.lifeflow.domain.core

enum class TierTruthProvenance {
    LOCAL_SEED,
    LOCAL_DATASTORE,
    MIGRATED_SHARED_PREFERENCES,
    SERVER_VERIFIED,
    SERVER_GRACE,
    SERVER_REVOKED,
    LOCAL_LOCKED,
    FALLBACK_DEFAULT
}
