package com.lifeflow.domain.usecase

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

class CreateIdentityUseCase(
    private val repository: IdentityRepository
) {

    suspend operator fun invoke(timestamp: Long): LifeFlowIdentity {
        val identity = LifeFlowIdentity(
            id = UUID.randomUUID(),
            createdAtEpochMillis = timestamp,
            isActive = true
        )

        repository.save(identity)
        return identity
    }
}