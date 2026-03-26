package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import org.junit.Assert.*
import org.junit.Test

class DashboardStateTest {

    @Test
    fun `resolveDashboardState returns HC_UNAVAILABLE when not installed`() {
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.NotInstalled,
            requiredCount = 2,
            grantedCount = 0,
            digitalTwinState = null,
            wellbeingAssessment = null
        )
        assertEquals(DashboardState.HC_UNAVAILABLE, state)
    }

    @Test
    fun `resolveDashboardState returns HC_UNAVAILABLE when update required`() {
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.UpdateRequired,
            requiredCount = 2,
            grantedCount = 0,
            digitalTwinState = null,
            wellbeingAssessment = null
        )
        assertEquals(DashboardState.HC_UNAVAILABLE, state)
    }

    @Test
    fun `resolveDashboardState returns NEEDS_PERMISSIONS when missing`() {
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 1,
            digitalTwinState = null,
            wellbeingAssessment = null
        )
        assertEquals(DashboardState.NEEDS_PERMISSIONS, state)
    }

    @Test
    fun `resolveDashboardState returns LOADING when no twin state`() {
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2,
            digitalTwinState = null,
            wellbeingAssessment = null
        )
        assertEquals(DashboardState.LOADING, state)
    }

    @Test
    fun `resolveDashboardState returns NO_DATA when twin has no data`() {
        val twinState = createDigitalTwinState(
            stepsLast24h = null,
            avgHeartRateLast24h = null
        )
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2,
            digitalTwinState = twinState,
            wellbeingAssessment = null
        )
        assertEquals(DashboardState.NO_DATA, state)
    }

    @Test
    fun `resolveDashboardState returns ATTENTION when wellbeing needs attention`() {
        val twinState = createDigitalTwinState(
            stepsLast24h = 5000,
            avgHeartRateLast24h = 72
        )
        val wellbeing = WellbeingAssessment(
            activityLevel = ActivityLevel.MODERATE,
            heartRateStatus = HeartRateStatus.NORMAL,
            overallReadiness = OverallReadiness.ATTENTION_REQUIRED,
            notes = emptyList()
        )
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2,
            digitalTwinState = twinState,
            wellbeingAssessment = wellbeing
        )
        assertEquals(DashboardState.ATTENTION, state)
    }

    @Test
    fun `resolveDashboardState returns READY when all good`() {
        val twinState = createDigitalTwinState(
            stepsLast24h = 10000,
            avgHeartRateLast24h = 72
        )
        val wellbeing = WellbeingAssessment(
            activityLevel = ActivityLevel.ACTIVE,
            heartRateStatus = HeartRateStatus.NORMAL,
            overallReadiness = OverallReadiness.GOOD,
            notes = emptyList()
        )
        val state = resolveDashboardState(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2,
            digitalTwinState = twinState,
            wellbeingAssessment = wellbeing
        )
        assertEquals(DashboardState.READY, state)
    }

    // ── Welcome titles ──

    @Test
    fun `dashboardWelcomeTitle returns correct title for each state`() {
        assertEquals("You're on track", dashboardWelcomeTitle(DashboardState.READY))
        assertEquals("Check your wellbeing", dashboardWelcomeTitle(DashboardState.ATTENTION))
        assertEquals("Almost there", dashboardWelcomeTitle(DashboardState.NEEDS_PERMISSIONS))
        assertEquals("Health Connect needed", dashboardWelcomeTitle(DashboardState.HC_UNAVAILABLE))
    }

    // ── Primary actions ──

    @Test
    fun `dashboardPrimaryActionLabel returns null for READY`() {
        assertNull(dashboardPrimaryActionLabel(DashboardState.READY))
    }

    @Test
    fun `dashboardPrimaryActionLabel returns action for actionable states`() {
        assertNotNull(dashboardPrimaryActionLabel(DashboardState.NEEDS_PERMISSIONS))
        assertNotNull(dashboardPrimaryActionLabel(DashboardState.HC_UNAVAILABLE))
        assertNotNull(dashboardPrimaryActionLabel(DashboardState.LOADING))
    }

    private fun createDigitalTwinState(
        stepsLast24h: Long?,
        avgHeartRateLast24h: Long?
    ): DigitalTwinState {
        return DigitalTwinState(
            identityInitialized = true,
            stepsLast24h = stepsLast24h,
            avgHeartRateLast24h = avgHeartRateLast24h,
            stepsAvailability = DigitalTwinState.Availability.OK,
            heartRateAvailability = DigitalTwinState.Availability.OK,
            lastUpdatedEpochMillis = System.currentTimeMillis(),
            notes = emptyList()
        )
    }
}
