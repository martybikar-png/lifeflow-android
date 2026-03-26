package com.lifeflow.domain.wellbeing

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HolisticWellbeingNodeTest {

    private lateinit var node: HolisticWellbeingNode

    @Before
    fun setup() {
        node = HolisticWellbeingNode()
    }

    // ── Identity not initialized ──

    @Test
    fun `assess returns BLOCKED when identity not initialized`() {
        val state = createDigitalTwinState(identityInitialized = false)

        val result = node.assess(state)

        assertEquals(OverallReadiness.BLOCKED, result.overallReadiness)
        assertEquals(ActivityLevel.UNAVAILABLE, result.activityLevel)
        assertEquals(HeartRateStatus.UNAVAILABLE, result.heartRateStatus)
    }

    // ── Activity Level ──

    @Test
    fun `assess returns ACTIVE when steps above 10000`() {
        val state = createDigitalTwinState(
            stepsLast24h = 12000,
            stepsAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(ActivityLevel.ACTIVE, result.activityLevel)
    }

    @Test
    fun `assess returns MODERATE when steps between 5000 and 10000`() {
        val state = createDigitalTwinState(
            stepsLast24h = 7000,
            stepsAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(ActivityLevel.MODERATE, result.activityLevel)
    }

    @Test
    fun `assess returns LOW when steps between 1000 and 5000`() {
        val state = createDigitalTwinState(
            stepsLast24h = 2500,
            stepsAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(ActivityLevel.LOW, result.activityLevel)
    }

    @Test
    fun `assess returns SEDENTARY when steps below 1000`() {
        val state = createDigitalTwinState(
            stepsLast24h = 500,
            stepsAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(ActivityLevel.SEDENTARY, result.activityLevel)
    }

    @Test
    fun `assess returns NO_ACCESS when steps permission denied`() {
        val state = createDigitalTwinState(
            stepsAvailability = DigitalTwinState.Availability.PERMISSION_DENIED
        )

        val result = node.assess(state)

        assertEquals(ActivityLevel.NO_ACCESS, result.activityLevel)
    }

    // ── Heart Rate Status ──

    @Test
    fun `assess returns NORMAL when heart rate between 61 and 100`() {
        val state = createDigitalTwinState(
            avgHeartRateLast24h = 72,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(HeartRateStatus.NORMAL, result.heartRateStatus)
    }

    @Test
    fun `assess returns RESTING_LOW when heart rate between 40 and 60`() {
        val state = createDigitalTwinState(
            avgHeartRateLast24h = 55,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(HeartRateStatus.RESTING_LOW, result.heartRateStatus)
    }

    @Test
    fun `assess returns ABNORMAL_LOW when heart rate below 40`() {
        val state = createDigitalTwinState(
            avgHeartRateLast24h = 35,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(HeartRateStatus.ABNORMAL_LOW, result.heartRateStatus)
    }

    @Test
    fun `assess returns ABNORMAL_HIGH when heart rate above 120`() {
        val state = createDigitalTwinState(
            avgHeartRateLast24h = 130,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(HeartRateStatus.ABNORMAL_HIGH, result.heartRateStatus)
    }

    // ── Overall Readiness ──

    @Test
    fun `assess returns GOOD when active and normal heart rate`() {
        val state = createDigitalTwinState(
            stepsLast24h = 12000,
            avgHeartRateLast24h = 72,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(OverallReadiness.GOOD, result.overallReadiness)
    }

    @Test
    fun `assess returns FAIR when moderate activity`() {
        val state = createDigitalTwinState(
            stepsLast24h = 7000,
            avgHeartRateLast24h = 72,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(OverallReadiness.FAIR, result.overallReadiness)
    }

    @Test
    fun `assess returns LOW when sedentary`() {
        val state = createDigitalTwinState(
            stepsLast24h = 500,
            avgHeartRateLast24h = 72,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(OverallReadiness.LOW, result.overallReadiness)
    }

    @Test
    fun `assess returns ATTENTION_REQUIRED when heart rate abnormal`() {
        val state = createDigitalTwinState(
            stepsLast24h = 12000,
            avgHeartRateLast24h = 130,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertEquals(OverallReadiness.ATTENTION_REQUIRED, result.overallReadiness)
    }

    @Test
    fun `assess returns NO_ACCESS when all permissions denied`() {
        val state = createDigitalTwinState(
            stepsAvailability = DigitalTwinState.Availability.PERMISSION_DENIED,
            heartRateAvailability = DigitalTwinState.Availability.PERMISSION_DENIED
        )

        val result = node.assess(state)

        assertEquals(OverallReadiness.NO_ACCESS, result.overallReadiness)
    }

    // ── Notes ──

    @Test
    fun `assess adds sedentary note when very low activity`() {
        val state = createDigitalTwinState(
            stepsLast24h = 500,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertTrue(result.notes.any { it.contains("low", ignoreCase = true) })
    }

    @Test
    fun `assess adds note when abnormal heart rate`() {
        val state = createDigitalTwinState(
            avgHeartRateLast24h = 130,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK
        )

        val result = node.assess(state)

        assertTrue(result.notes.any { it.contains("elevated", ignoreCase = true) })
    }

    private fun createDigitalTwinState(
        identityInitialized: Boolean = true,
        stepsLast24h: Long? = null,
        avgHeartRateLast24h: Long? = null,
        stepsAvailability: DigitalTwinState.Availability = DigitalTwinState.Availability.OK,
        heartRateAvailability: DigitalTwinState.Availability = DigitalTwinState.Availability.OK
    ): DigitalTwinState {
        return DigitalTwinState(
            identityInitialized = identityInitialized,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h,
            stepsAvailability = stepsAvailability,
            heartRateAvailability = heartRateAvailability,
            lastUpdatedEpochMillis = System.currentTimeMillis(),
            notes = emptyList()
        )
    }
}
