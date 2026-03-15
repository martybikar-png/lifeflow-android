package com.lifeflow.domain.core

import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

/**
 * Test-only in-memory repo for domain tests (no dependency on :data module).
 *
 * Invariant:
 * - at most one identity is active at a time
 */
class InMemoryIdentityRepository : IdentityRepository {

    private val storage = LinkedHashMap<UUID, LifeFlowIdentity>()
    private var activeId: UUID? = null

    override suspend fun save(identity: LifeFlowIdentity) {
        if (identity.isActive) {
            deactivatePreviousActiveIfNeeded(newActiveId = identity.id)
            storage[identity.id] = identity
            activeId = identity.id
        } else {
            storage[identity.id] = identity
            if (activeId == identity.id) {
                activeId = null
            }
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return storage[id]
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        val id = activeId ?: return null
        val identity = storage[id] ?: return null

        return if (identity.isActive) {
            identity
        } else {
            activeId = null
            null
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        storage.remove(identity.id)
        if (activeId == identity.id) {
            activeId = null
        }
    }

    private fun deactivatePreviousActiveIfNeeded(newActiveId: UUID) {
        val previousActiveId = activeId ?: return
        if (previousActiveId == newActiveId) return

        val previous = storage[previousActiveId] ?: return
        storage[previousActiveId] = previous.copy(isActive = false)
    }
}