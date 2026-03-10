package com.lifeflow.data.wellbeing

import android.content.Context
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.domain.wellbeing.WellbeingRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HealthConnectWellbeingRepositoryInstrumentedTest {

    private val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    private val repository: WellbeingRepository
        get() = HealthConnectWellbeingRepository(appContext)

    @Test
    fun getSdkStatusReturnsKnownDomainEnum() {
        val status = repository.getSdkStatus()

        assertTrue(
            status == WellbeingRepository.SdkStatus.Available ||
                status == WellbeingRepository.SdkStatus.NotInstalled ||
                status == WellbeingRepository.SdkStatus.NotSupported ||
                status == WellbeingRepository.SdkStatus.UpdateRequired
        )
    }

    @Test
    fun requiredPermissionsReturnsExpectedReadPermissions() {
        val permissions = repository.requiredPermissions()

        assertEquals(
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class)
            ),
            permissions
        )
    }

    @Test
    fun readTotalStepsRejectsInvalidRangeBeforeGatewayAccess() = runBlocking {
        val start = Instant.parse("2026-03-10T10:00:00Z")
        val end = Instant.parse("2026-03-10T09:00:00Z")

        try {
            repository.readTotalSteps(start, end)
            throw AssertionError("Expected IllegalArgumentException for invalid time range.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("end must be >= start") == true)
        }
    }

    @Test
    fun readAvgHeartRateRejectsInvalidRangeBeforeGatewayAccess() = runBlocking {
        val start = Instant.parse("2026-03-10T10:00:00Z")
        val end = Instant.parse("2026-03-10T09:00:00Z")

        try {
            repository.readAvgHeartRateBpm(start, end)
            throw AssertionError("Expected IllegalArgumentException for invalid time range.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("end must be >= start") == true)
        }
    }
}