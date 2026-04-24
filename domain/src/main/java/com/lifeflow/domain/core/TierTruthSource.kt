package com.lifeflow.domain.core

/**
 * TierTruthSource — source of truth for the currently effective tier state.
 *
 * Domain depends only on this boundary, not on Android storage APIs.
 * The implementation can later move between local persistence and
 * server-backed entitlement truth without changing TierManager callers.
 */
interface TierTruthSource {
    fun currentSnapshotOrNull(): TierTruthSnapshot?
    fun persistSnapshot(snapshot: TierTruthSnapshot)
    fun clear()
}
