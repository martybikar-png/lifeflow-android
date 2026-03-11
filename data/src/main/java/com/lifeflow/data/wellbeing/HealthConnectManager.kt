package com.lifeflow.data.wellbeing

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

internal object HealthConnectManager {

    private const val NO_STEPS_DATA = -1L

    enum class SdkStatus { Available, NotInstalled, NotSupported, UpdateRequired }

    fun getSdkStatus(context: Context): SdkStatus {
        val status = HealthConnectClient.getSdkStatus(context.applicationContext)

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
        HealthConnectClient.getOrCreate(context.applicationContext)

    suspend fun readTotalSteps(
        context: Context,
        start: Instant,
        end: Instant
    ): Long {
        requireValidRange(start, end)

        val hc = client(context)
        val timeRange = TimeRangeFilter.between(start, end)

        val aggregateValue = hc.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = timeRange
            )
        )[StepsRecord.COUNT_TOTAL]

        if (aggregateValue != null) {
            return aggregateValue
        }

        return readTotalStepsFallback(hc, timeRange)
    }

    suspend fun readAvgHeartRateBpm(
        context: Context,
        start: Instant,
        end: Instant
    ): Double? {
        requireValidRange(start, end)

        val hc = client(context)
        val timeRange = TimeRangeFilter.between(start, end)

        val aggregateValue = hc.aggregate(
            AggregateRequest(
                metrics = setOf(HeartRateRecord.BPM_AVG),
                timeRangeFilter = timeRange
            )
        )[HeartRateRecord.BPM_AVG]?.toDouble()

        normalizeFiniteDouble(aggregateValue)?.let { return it }

        return readAvgHeartRateFallback(hc, timeRange)
    }

    suspend fun getGrantedPermissions(context: Context): Set<String> {
        val hc = client(context)
        return hc.permissionController.getGrantedPermissions()
    }

    private suspend fun readTotalStepsFallback(
        hc: HealthConnectClient,
        timeRange: TimeRangeFilter
    ): Long {
        val response = hc.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRange
            )
        )

        if (response.records.isEmpty()) {
            return NO_STEPS_DATA
        }

        return response.records.fold(0L) { acc, record -> acc + record.count }
    }

    private suspend fun readAvgHeartRateFallback(
        hc: HealthConnectClient,
        timeRange: TimeRangeFilter
    ): Double? {
        val response = hc.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRange
            )
        )

        val samples = response.records
            .flatMap { record -> record.samples }
            .mapNotNull { sample ->
                sample.beatsPerMinute
                    .toDouble()
                    .takeIf { it.isFinite() && it > 0.0 }
            }

        return if (samples.isEmpty()) null else samples.average()
    }

    private fun normalizeFiniteDouble(value: Double?): Double? {
        return when {
            value == null -> null
            value.isNaN() -> null
            value.isInfinite() -> null
            else -> value
        }
    }

    private fun requireValidRange(start: Instant, end: Instant) {
        require(!end.isBefore(start)) {
            "Invalid Health Connect time range: end must be >= start"
        }
    }
}