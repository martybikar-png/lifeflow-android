package com.lifeflow

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.wellbeing.WellbeingRepository
import java.time.Instant
import java.util.UUID

internal class FakeIdentityRepository(
    initialActive: LifeFlowIdentity? = null,
    private val throwsOnGetActive: Boolean = false,
    private val throwsOnSave: Boolean = false
) : IdentityRepository {

    var activeIdentity: LifeFlowIdentity? = initialActive
        private set

    var saveCalls: Int = 0
        private set

    override suspend fun save(identity: LifeFlowIdentity) {
        saveCalls++
        if (throwsOnSave) {
            throw IllegalStateException("identity save failed")
        }
        activeIdentity = identity
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return activeIdentity?.takeIf { it.id == id }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        if (throwsOnGetActive) {
            throw IllegalStateException("active identity lookup failed")
        }
        return activeIdentity
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        if (activeIdentity?.id == identity.id) {
            activeIdentity = null
        }
    }
}

internal class FakeWellbeingRepository(
    var sdkStatusValue: WellbeingRepository.SdkStatus,
    var requiredPermissionsValue: Set<String>,
    var grantedPermissionsValue: Set<String>,
    var stepsValue: Long,
    var avgHeartRateValue: Double?,
    var throwsOnRequiredPermissions: Boolean = false,
    var throwsOnGrantedPermissions: Boolean = false,
    var throwsOnReadSteps: Boolean = false,
    var throwsOnReadHeartRate: Boolean = false
) : WellbeingRepository {

    override fun getSdkStatus(): WellbeingRepository.SdkStatus = sdkStatusValue

    override fun requiredPermissions(): Set<String> {
        if (throwsOnRequiredPermissions) {
            throw IllegalStateException("required permissions unavailable")
        }
        return requiredPermissionsValue
    }

    override suspend fun grantedPermissions(): Set<String> {
        if (throwsOnGrantedPermissions) {
            throw IllegalStateException("granted permissions unavailable")
        }
        return grantedPermissionsValue
    }

    override suspend fun readTotalSteps(start: Instant, end: Instant): Long {
        if (throwsOnReadSteps) {
            throw IllegalStateException("steps read failed")
        }
        return stepsValue
    }

    override suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double? {
        if (throwsOnReadHeartRate) {
            throw IllegalStateException("heart-rate read failed")
        }
        return avgHeartRateValue
    }
}