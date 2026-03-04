package com.lifeflow.data.wellbeing

import android.content.Context
import com.lifeflow.domain.wellbeing.WellbeingRepository
import java.time.Instant

class HealthConnectWellbeingRepository(
    context: Context
) : WellbeingRepository {

    private val appContext = context.applicationContext

    override fun getSdkStatus(): WellbeingRepository.SdkStatus {
        return when (HealthConnectManager.getSdkStatus(appContext)) {
            HealthConnectManager.SdkStatus.Available -> WellbeingRepository.SdkStatus.Available
            HealthConnectManager.SdkStatus.NotInstalled -> WellbeingRepository.SdkStatus.NotInstalled
            HealthConnectManager.SdkStatus.NotSupported -> WellbeingRepository.SdkStatus.NotSupported
            HealthConnectManager.SdkStatus.UpdateRequired -> WellbeingRepository.SdkStatus.UpdateRequired
        }
    }

    override fun requiredPermissions(): Set<String> = HealthConnectManager.permissions()

    override suspend fun grantedPermissions(): Set<String> =
        HealthConnectManager.getGrantedPermissions(appContext)

    override suspend fun readTotalSteps(start: Instant, end: Instant): Long =
        HealthConnectManager.readTotalSteps(appContext, start, end)

    override suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double? =
        HealthConnectManager.readAvgHeartRateBpm(appContext, start, end)
}