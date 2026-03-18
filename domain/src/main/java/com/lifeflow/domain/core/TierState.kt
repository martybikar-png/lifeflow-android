package com.lifeflow.domain.core

/**
 * TierState — defines which tier the user is operating in.
 *
 * FREE: Local-only mode. No Orchestrator, no Digital Twin,
 *       no biometric vault, no module data access.
 *       AI Shadow Diary on-device only (basic).
 *       No cloud, no sync.
 *
 * CORE: Full Life OS. All modules, orchestrator, vault,
 *       biometric auth, Digital Twin, cross-module intelligence.
 *
 * V1: tier is determined locally — no server validation yet.
 * Future: server-side entitlement check.
 */
enum class TierState {
    FREE,
    CORE
}
