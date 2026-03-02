package com.lifeflow.domain.core

import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

/**
 * Test-only in-memory repo for domain tests (no dependency on :data module).
 */
class InMemoryIdentityRepository : IdentityRepository {

    private val storage = LinkedHashMap<UUID, LifeFlowIdentity>()
    private var activeId: UUID? = null

    override suspend fun save(identity: LifeFlowIdentity) {
        storage[identity.id] = identity
        if (identity.isActive) {
            activeId = identity.id
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return storage[id]
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        val id = activeId ?: return null
        return storage[id]
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        storage.remove(identity.id)
        if (activeId == identity.id) {
            activeId = null
        }
    }
}