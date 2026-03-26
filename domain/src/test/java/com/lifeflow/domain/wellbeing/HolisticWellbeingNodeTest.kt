package com.lifeflow.domain.wellbeing

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HolisticWellbeingNodeTest {

    private val node = HolisticWellbeingNode()

    private fun state(
        identityInitialized: Boolean = true,
        steps: Long? = null,
        heartRate: Long? = null,
        stepsAvailability: DigitalTwinState.Availability = DigitalTwinState.Availability.OK,
        heartRateAvailability: DigitalTwinState.Availability = DigitalTwinState.Availability.OK
    ) = DigitalTwinState(
        identityInitialized = identityInitialized,
        stepsLast24h = steps,
        avgHeartRateLast24h = heartRate,
        lastUpdatedEpochMillis = 0L,
        stepsAvailability = stepsAvailability,
        heartRateAvailability = heartRateAvailability
    )

    @Test
    fun `identity not initialized returns BLOCKED`() {
        val result = node.assess(state(identityInitialized = false))
        assertEquals(OverallReadiness.BLOCKED, result.overallReadiness)
        assertEquals(ActivityLevel.UNAVAILABLE, result.activityLevel)
        assertEquals(HeartRateStatus.UNAVAILABLE, result.heartRateStatus)
    }

    @Test
    fun `active steps returns ACTIVE and GOOD readiness`() {
        val result = node.assess(state(steps = 12_000, heartRate = 72))
        assertEquals(ActivityLevel.ACTIVE, result.activityLevel)
        assertEquals(HeartRateStatus.NORMAL, result.heartRateStatus)
        assertEquals(OverallReadiness.GOOD, result.overallReadiness)
    }

    @Test
    fun `moderate steps returns MODERATE and FAIR readiness`() {
        val result = node.assess(state(steps = 7_000, heartRate = 72))
        assertEquals(ActivityLevel.MODERATE, result.activityLevel)
        assertEquals(OverallReadiness.FAIR, result.overallReadiness)
    }

    @Test
    fun `sedentary steps returns SEDENTARY and LOW readiness`() {
        val result = node.assess(state(steps = 500, heartRate = 72))
        assertEquals(ActivityLevel.SEDENTARY, result.activityLevel)
        assertEquals(OverallReadiness.LOW, result.overallReadiness)
    }

    @Test
    fun `abnormal high heart rate returns ATTENTION_REQUIRED`() {
        val result = node.assess(state(steps = 8_000, heartRate = 130))
        assertEquals(HeartRateStatus.ABNORMAL_HIGH, result.heartRateStatus)
        assertEquals(OverallReadiness.ATTENTION_REQUIRED, result.overallReadiness)
    }

    @Test
    fun `abnormal low heart rate returns ATTENTION_REQUIRED`() {
        val result = node.assess(state(steps = 8_000, heartRate = 35))
        assertEquals(HeartRateStatus.ABNORMAL_LOW, result.heartRateStatus)
        assertEquals(OverallReadiness.ATTENTION_REQUIRED, result.overallReadiness)
    }

    @Test
    fun `permission denied both returns NO_ACCESS`() {
        val result = node.assess(
            state(
                stepsAvailability = DigitalTwinState.Availability.PERMISSION_DENIED,
                heartRateAvailability = DigitalTwinState.Availability.PERMISSION_DENIED
            )
        )
        assertEquals(ActivityLevel.NO_ACCESS, result.activityLevel)
        assertEquals(HeartRateStatus.NO_ACCESS, result.heartRateStatus)
        assertEquals(OverallReadiness.NO_ACCESS, result.overallReadiness)
    }

    @Test
    fun `no data availability returns INSUFFICIENT_DATA`() {
        val result = node.assess(
            state(
                stepsAvailability = DigitalTwinState.Availability.NO_DATA,
                heartRateAvailability = DigitalTwinState.Availability.NO_DATA
            )
        )
        assertEquals(OverallReadiness.INSUFFICIENT_DATA, result.overallReadiness)
    }

    @Test
    fun `blocked single signal returns BLOCKED readiness`() {
        val result = node.assess(
            state(
                stepsAvailability = DigitalTwinState.Availability.BLOCKED,
                heartRate = 72,
                heartRateAvailability = DigitalTwinState.Availability.OK
            )
        )
        assertEquals(ActivityLevel.UNAVAILABLE, result.activityLevel)
        assertEquals(HeartRateStatus.NORMAL, result.heartRateStatus)
        assertEquals(OverallReadiness.BLOCKED, result.overallReadiness)
    }

    @Test
    fun `sedentary assessment adds movement note`() {
        val result = node.assess(state(steps = 500, heartRate = 72))
        assertTrue(
            result.notes.any { it.contains("very low", ignoreCase = true) }
        )
    }

    @Test
    fun `abnormal high heart rate adds elevated note`() {
        val result = node.assess(state(steps = 8_000, heartRate = 130))
        assertTrue(
            result.notes.any { it.contains("elevated", ignoreCase = true) }
        )
    }

    @Test
    fun `insufficient data adds refresh later note`() {
        val result = node.assess(
            state(
                stepsAvailability = DigitalTwinState.Availability.NO_DATA,
                heartRateAvailability = DigitalTwinState.Availability.NO_DATA
            )
        )
        assertTrue(
            result.notes.any { it.contains("refresh later", ignoreCase = true) }
        )
    }
}
