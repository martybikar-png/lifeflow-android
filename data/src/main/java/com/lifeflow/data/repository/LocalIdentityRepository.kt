package com.lifeflow.data.repository

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class LocalIdentityRepository : IdentityRepository {

    private val mutex = Mutex()
    private val identities = mutableListOf<LifeFlowIdentity>()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            identities.removeAll { it.id == identity.id }
            identities.add(identity)
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            identities.find { it.id == id }
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            identities.find { it.isActive }
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            identities.removeAll { it.id == identity.id }
        }
    }
}