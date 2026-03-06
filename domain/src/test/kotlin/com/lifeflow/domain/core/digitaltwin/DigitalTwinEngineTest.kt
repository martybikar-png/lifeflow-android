package com.lifeflow.domain.core.digitaltwin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DigitalTwinEngineTest {

    private val engine = DigitalTwinEngine()

    @Test
    fun `returns BLOCKED and hides metrics when identity is not initialized`() {
        val state = engine.computeState(
            identityInitialized = false,
            stepsLast24h = 1234L,
            avgHeartRateLast24h = 72L,
            stepsPermissionGranted = true,
            heartRatePermissionGranted = true
        )

        assertEquals(false, state.identityInitialized)
        assertEquals(DigitalTwinState.Availability.BLOCKED, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.BLOCKED, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
        assertTrue(state.lastUpdatedEpochMillis > 0)
        assertTrue(
            state.notes.any { it.contains("blocked", ignoreCase = true) },
            "Expected a diagnostic note mentioning blocked state."
        )
    }

    @Test
    fun `returns UNKNOWN and hides metrics when permission state is not resolved`() {
        val state = engine.computeState(
            identityInitialized = true,
            stepsLast24h = 1234L,
            avgHeartRateLast24h = 72L,
            stepsPermissionGranted = null,
            heartRatePermissionGranted = null
        )

        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
        assertTrue(
            state.notes.any { it.contains("unknown", ignoreCase = true) || it.contains("not resolved", ignoreCase = true) },
            "Expected a diagnostic note mentioning unresolved permission state."
        )
    }

    @Test
    fun `resolves permissions independently per metric`() {
        val state = engine.computeState(
            identityInitialized = true,
            stepsLast24h = 2500L,
            avgHeartRateLast24h = 68L,
            stepsPermissionGranted = true,
            heartRatePermissionGranted = false
        )

        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, state.heartRateAvailability)
        assertEquals(2500L, state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
        assertTrue(
            state.notes.any { it.contains("Heart-rate permission denied", ignoreCase = true) },
            "Expected a diagnostic note for heart-rate permission denial."
        )
    }

    @Test
    fun `returns NO_DATA when permission is granted but metric is absent`() {
        val state = engine.computeState(
            identityInitialized = true,
            stepsLast24h = null,
            avgHeartRateLast24h = null,
            stepsPermissionGranted = true,
            heartRatePermissionGranted = true
        )

        assertEquals(DigitalTwinState.Availability.NO_DATA, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `treats zero steps as valid OK data`() {
        val state = engine.computeState(
            identityInitialized = true,
            stepsLast24h = 0L,
            avgHeartRateLast24h = null,
            stepsPermissionGranted = true,
            heartRatePermissionGranted = true
        )

        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(0L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `treats non positive heart rate as NO_DATA and hides value`() {
        val state = engine.computeState(
            identityInitialized = true,
            stepsLast24h = 1800L,
            avgHeartRateLast24h = 0L,
            stepsPermissionGranted = true,
            heartRatePermissionGranted = true
        )

        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(1800L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)
        assertTrue(
            state.notes.any { it.contains("Heart-rate<=0", ignoreCase = true) || it.contains("invalid", ignoreCase = true) },
            "Expected a diagnostic note for invalid/non-positive heart rate."
        )
    }
}