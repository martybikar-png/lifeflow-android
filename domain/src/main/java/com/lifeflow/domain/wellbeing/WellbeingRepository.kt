package com.lifeflow.domain.wellbeing

import java.time.Instant

interface WellbeingRepository {

    enum class SdkStatus { Available, NotInstalled, NotSupported, UpdateRequired }

    fun getSdkStatus(): SdkStatus

    fun requiredPermissions(): Set<String>

    suspend fun grantedPermissions(): Set<String>

    suspend fun readTotalSteps(start: Instant, end: Instant): Long

    suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double?
}