package com.lifeflow.data.wellbeing

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

internal object HealthConnectManager {

    enum class SdkStatus { Available, NotInstalled, NotSupported, UpdateRequired }

    fun getSdkStatus(context: Context): SdkStatus {
        val status = HealthConnectClient.getSdkStatus(context)

        // Use reflection so we compile even if some constants are missing in the installed dependency.
        val sdkAvailable = getIntConstOrNull("SDK_AVAILABLE")
        val providerUpdateRequired = getIntConstOrNull("SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED")
        val providerNotInstalled = getIntConstOrNull("SDK_UNAVAILABLE_PROVIDER_NOT_INSTALLED")

        return when {
            sdkAvailable != null && status == sdkAvailable -> SdkStatus.Available
            providerUpdateRequired != null && status == providerUpdateRequired -> SdkStatus.UpdateRequired
            providerNotInstalled != null && status == providerNotInstalled -> SdkStatus.NotInstalled
            else -> SdkStatus.NotSupported
        }
    }

    private fun getIntConstOrNull(fieldName: String): Int? {
        return try {
            HealthConnectClient::class.java.getField(fieldName).getInt(null)
        } catch (_: Throwable) {
            null
        }
    }

    fun permissions(): Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    fun client(context: Context): HealthConnectClient =
        HealthConnectClient.getOrCreate(context)

    suspend fun readTotalSteps(
        context: Context,
        start: Instant,
        end: Instant
    ): Long {
        val hc = client(context)
        val response = hc.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readAvgHeartRateBpm(
        context: Context,
        start: Instant,
        end: Instant
    ): Double? {
        val hc = client(context)
        val response = hc.aggregate(
            AggregateRequest(
                metrics = setOf(HeartRateRecord.BPM_AVG),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        val value = response[HeartRateRecord.BPM_AVG]
        return value?.toDouble()
    }

    suspend fun getGrantedPermissions(context: Context): Set<String> {
        val hc = client(context)
        return hc.permissionController.getGrantedPermissions()
    }
}