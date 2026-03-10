package com.lifeflow.data.wellbeing

import android.content.Context
import com.lifeflow.domain.wellbeing.WellbeingRepository
import java.time.Instant

class HealthConnectWellbeingRepository private constructor(
    context: Context,
    private val gateway: Gateway,
    @Suppress("UNUSED_PARAMETER") useInjectedGateway: Boolean
) : WellbeingRepository {

    private val appContext = context.applicationContext

    constructor(context: Context) : this(
        context = context,
        gateway = DefaultGateway,
        useInjectedGateway = false
    )

    internal constructor(
        context: Context,
        gateway: Gateway
    ) : this(
        context = context,
        gateway = gateway,
        useInjectedGateway = true
    )

    override fun getSdkStatus(): WellbeingRepository.SdkStatus {
        return mapSdkStatus(gateway.getSdkStatus(appContext))
    }

    override fun requiredPermissions(): Set<String> =
        gateway.permissions()

    override suspend fun grantedPermissions(): Set<String> =
        gateway.getGrantedPermissions(appContext)

    override suspend fun readTotalSteps(start: Instant, end: Instant): Long {
        requireValidRange(start, end)
        return gateway.readTotalSteps(appContext, start, end)
    }

    override suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double? {
        requireValidRange(start, end)
        val value = gateway.readAvgHeartRateBpm(appContext, start, end)
        return normalizeHeartRate(value)
    }

    private fun mapSdkStatus(
        status: HealthConnectManager.SdkStatus
    ): WellbeingRepository.SdkStatus {
        return when (status) {
            HealthConnectManager.SdkStatus.Available -> WellbeingRepository.SdkStatus.Available
            HealthConnectManager.SdkStatus.NotInstalled -> WellbeingRepository.SdkStatus.NotInstalled
            HealthConnectManager.SdkStatus.NotSupported -> WellbeingRepository.SdkStatus.NotSupported
            HealthConnectManager.SdkStatus.UpdateRequired -> WellbeingRepository.SdkStatus.UpdateRequired
        }
    }

    private fun requireValidRange(start: Instant, end: Instant) {
        require(!end.isBefore(start)) {
            "Invalid wellbeing time range: end must be >= start"
        }
    }

    private fun normalizeHeartRate(value: Double?): Double? {
        return when {
            value == null -> null
            value.isNaN() -> null
            value.isInfinite() -> null
            else -> value
        }
    }

    internal interface Gateway {
        fun getSdkStatus(context: Context): HealthConnectManager.SdkStatus
        fun permissions(): Set<String>
        suspend fun getGrantedPermissions(context: Context): Set<String>
        suspend fun readTotalSteps(context: Context, start: Instant, end: Instant): Long
        suspend fun readAvgHeartRateBpm(context: Context, start: Instant, end: Instant): Double?
    }

    private object DefaultGateway : Gateway {
        override fun getSdkStatus(context: Context): HealthConnectManager.SdkStatus =
            HealthConnectManager.getSdkStatus(context)

        override fun permissions(): Set<String> =
            HealthConnectManager.permissions()

        override suspend fun getGrantedPermissions(context: Context): Set<String> =
            HealthConnectManager.getGrantedPermissions(context)

        override suspend fun readTotalSteps(
            context: Context,
            start: Instant,
            end: Instant
        ): Long = HealthConnectManager.readTotalSteps(context, start, end)

        override suspend fun readAvgHeartRateBpm(
            context: Context,
            start: Instant,
            end: Instant
        ): Double? = HealthConnectManager.readAvgHeartRateBpm(context, start, end)
    }
}