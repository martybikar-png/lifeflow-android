package com.lifeflow.domain.usecase

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity

class GetActiveIdentityUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(): LifeFlowIdentity? {
        return repository.getActiveIdentity()
    }
}