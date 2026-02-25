package com.lifeflow.domain.core

import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

/**
 * Contract for identity persistence.
 * No implementation here — domain only defines behavior.
 */
interface IdentityRepository {

    suspend fun save(identity: LifeFlowIdentity)

    suspend fun getById(id: UUID): LifeFlowIdentity?

    suspend fun getActiveIdentity(): LifeFlowIdentity?

    suspend fun delete(identity: LifeFlowIdentity)
}