package com.lifeflow.domain.model

import java.util.UUID

/**
 * Core identity of a LifeFlow user.
 * Pure domain model — no Android dependencies.
 */
data class LifeFlowIdentity(

    val id: UUID = UUID.randomUUID(),

    val createdAtEpochMillis: Long,

    val isBiometricProtected: Boolean = false,

    val vaultInitialized: Boolean = false,

    val isActive: Boolean = true
)