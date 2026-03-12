package com.lifeflow.data.wellbeing

import android.content.Context
import android.content.ContextWrapper
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class HealthConnectWellbeingRepositoryTest {

    private val testContext = FakeContext()

    @Test
    fun `getSdkStatus maps all gateway statuses to domain statuses`() {
        val gateway = FakeGateway()
        val repository = newRepository(gateway)

        val mappings = listOf(
            HealthConnectManager.SdkStatus.Available to WellbeingRepository.SdkStatus.Available,
            HealthConnectManager.SdkStatus.NotInstalled to WellbeingRepository.SdkStatus.NotInstalled,
            HealthConnectManager.SdkStatus.NotSupported to WellbeingRepository.SdkStatus.NotSupported,
            HealthConnectManager.SdkStatus.UpdateRequired to WellbeingRepository.SdkStatus.UpdateRequired
        )

        for ((gatewayStatus, expectedDomainStatus) in mappings) {
            gateway.sdkStatusValue = gatewayStatus
            assertEquals(expectedDomainStatus, repository.getSdkStatus())
        }
    }

    @Test
    fun `requiredPermissions delegates directly to gateway`() {
        val gateway = FakeGateway().apply {
            permissionsValue = setOf("perm.steps", "perm.heart")
        }
        val repository = newRepository(gateway)

        val result = repository.requiredPermissions()

        assertEquals(setOf("perm.steps", "perm.heart"), result)
        assertEquals(1, gateway.permissionsCalls)
    }

    @Test
    fun `grantedPermissions delegates using application context`() {
        val gateway = FakeGateway().apply {
            grantedPermissionsValue = setOf("perm.steps")
        }
        val repository = newRepository(gateway)

        val result = runSuspend { repository.grantedPermissions() }

        assertEquals(setOf("perm.steps"), result)
        assertSame(testContext, gateway.lastGrantedPermissionsContext)
    }

    @Test
    fun `readTotalSteps rejects invalid time range before calling gateway`() {
        val gateway = FakeGateway().apply {
            totalStepsValue = 1234L
        }
        val repository = newRepository(gateway)

        val start = Instant.parse("2026-03-09T10:00:00Z")
        val end = Instant.parse("2026-03-09T09:00:00Z")

        try {
            runSuspend { repository.readTotalSteps(start, end) }
            throw AssertionError("Expected IllegalArgumentException for invalid range.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("end must be >= start"))
        }

        assertEquals(0, gateway.readTotalStepsCalls)
    }

    @Test
    fun `readAvgHeartRateBpm rejects invalid time range before calling gateway`() {
        val gateway = FakeGateway().apply {
            avgHeartRateValue = 71.6
        }
        val repository = newRepository(gateway)

        val start = Instant.parse("2026-03-09T10:00:00Z")
        val end = Instant.parse("2026-03-09T09:00:00Z")

        try {
            runSuspend { repository.readAvgHeartRateBpm(start, end) }
            throw AssertionError("Expected IllegalArgumentException for invalid range.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("end must be >= start"))
        }

        assertEquals(0, gateway.readAvgHeartRateCalls)
    }

    @Test
    fun `readTotalSteps preserves repository sentinel and valid zero values`() {
        val gateway = FakeGateway()
        val repository = newRepository(gateway)

        val start = Instant.parse("2026-03-09T08:00:00Z")
        val end = Instant.parse("2026-03-09T10:00:00Z")

        gateway.totalStepsValue = -1L
        assertEquals(-1L, runSuspend { repository.readTotalSteps(start, end) })

        gateway.totalStepsValue = 0L
        assertEquals(0L, runSuspend { repository.readTotalSteps(start, end) })

        assertSame(testContext, gateway.lastReadStepsContext)
        assertEquals(start, gateway.lastReadStepsStart)
        assertEquals(end, gateway.lastReadStepsEnd)
    }

    @Test
    fun `readAvgHeartRateBpm normalizes NaN and Infinity to null`() {
        val gateway = FakeGateway()
        val repository = newRepository(gateway)

        val start = Instant.parse("2026-03-09T08:00:00Z")
        val end = Instant.parse("2026-03-09T10:00:00Z")

        gateway.avgHeartRateValue = Double.NaN
        assertNull(runSuspend { repository.readAvgHeartRateBpm(start, end) })

        gateway.avgHeartRateValue = Double.POSITIVE_INFINITY
        assertNull(runSuspend { repository.readAvgHeartRateBpm(start, end) })

        gateway.avgHeartRateValue = Double.NEGATIVE_INFINITY
        assertNull(runSuspend { repository.readAvgHeartRateBpm(start, end) })
    }

    @Test
    fun `readAvgHeartRateBpm returns finite value unchanged`() {
        val gateway = FakeGateway().apply {
            avgHeartRateValue = 71.6
        }
        val repository = newRepository(gateway)

        val start = Instant.parse("2026-03-09T08:00:00Z")
        val end = Instant.parse("2026-03-09T10:00:00Z")

        val result = runSuspend { repository.readAvgHeartRateBpm(start, end) }

        assertEquals(71.6, result!!, 0.0)
        assertSame(testContext, gateway.lastReadHeartRateContext)
        assertEquals(start, gateway.lastReadHeartRateStart)
        assertEquals(end, gateway.lastReadHeartRateEnd)
    }

    private fun newRepository(
        gateway: HealthConnectWellbeingRepository.Gateway
    ): HealthConnectWellbeingRepository {
        val clazz = HealthConnectWellbeingRepository::class.java

        val twoArgConstructor = clazz.declaredConstructors.firstOrNull { constructor ->
            val types = constructor.parameterTypes
            types.size == 2 &&
                Context::class.java.isAssignableFrom(types[0])
        }

        if (twoArgConstructor != null) {
            twoArgConstructor.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return twoArgConstructor.newInstance(
                testContext,
                gateway
            ) as HealthConnectWellbeingRepository
        }

        val threeArgConstructor = clazz.declaredConstructors.firstOrNull { constructor ->
            val types = constructor.parameterTypes
            types.size == 3 &&
                Context::class.java.isAssignableFrom(types[0]) &&
                types[2] == Boolean::class.javaPrimitiveType
        } ?: throw AssertionError(
            "Could not find injectable HealthConnectWellbeingRepository constructor."
        )

        threeArgConstructor.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return threeArgConstructor.newInstance(
            testContext,
            gateway,
            true
        ) as HealthConnectWellbeingRepository
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var value: T? = null
        var failure: Throwable? = null

        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                result
                    .onSuccess { value = it }
                    .onFailure { failure = it }
            }
        })

        failure?.let { throw it }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    private class FakeGateway : HealthConnectWellbeingRepository.Gateway {
        var sdkStatusValue: HealthConnectManager.SdkStatus =
            HealthConnectManager.SdkStatus.Available

        var permissionsValue: Set<String> = emptySet()
        var grantedPermissionsValue: Set<String> = emptySet()
        var totalStepsValue: Long = 0L
        var avgHeartRateValue: Double? = null

        var permissionsCalls: Int = 0
        var readTotalStepsCalls: Int = 0
        var readAvgHeartRateCalls: Int = 0

        var lastGrantedPermissionsContext: Context? = null
        var lastReadStepsContext: Context? = null
        var lastReadStepsStart: Instant? = null
        var lastReadStepsEnd: Instant? = null
        var lastReadHeartRateContext: Context? = null
        var lastReadHeartRateStart: Instant? = null
        var lastReadHeartRateEnd: Instant? = null

        override fun getSdkStatus(context: Context): HealthConnectManager.SdkStatus {
            return sdkStatusValue
        }

        override fun permissions(): Set<String> {
            permissionsCalls++
            return permissionsValue
        }

        override suspend fun getGrantedPermissions(context: Context): Set<String> {
            lastGrantedPermissionsContext = context
            return grantedPermissionsValue
        }

        override suspend fun readTotalSteps(
            context: Context,
            start: Instant,
            end: Instant
        ): Long {
            readTotalStepsCalls++
            lastReadStepsContext = context
            lastReadStepsStart = start
            lastReadStepsEnd = end
            return totalStepsValue
        }

        override suspend fun readAvgHeartRateBpm(
            context: Context,
            start: Instant,
            end: Instant
        ): Double? {
            readAvgHeartRateCalls++
            lastReadHeartRateContext = context
            lastReadHeartRateStart = start
            lastReadHeartRateEnd = end
            return avgHeartRateValue
        }
    }

    private class FakeContext : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }
}