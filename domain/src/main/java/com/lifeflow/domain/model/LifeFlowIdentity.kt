package com.lifeflow.domain.model

import java.util.UUID

data class LifeFlowIdentity(
    val id: UUID,
    val createdAtEpochMillis: Long,
    val isActive: Boolean
)