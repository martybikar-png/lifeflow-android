package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import org.junit.Assert.*
import org.junit.Test

class AppScreenActionLogicTest {

    // ── hasMissingHealthPermissions ──

    @Test
    fun `hasMissingHealthPermissions returns true when granted less than required`() {
        assertTrue(hasMissingHealthPermissions(requiredCount = 2, grantedCount = 1))
    }

    @Test
    fun `hasMissingHealthPermissions returns false when all granted`() {
        assertFalse(hasMissingHealthPermissions(requiredCount = 2, grantedCount = 2))
    }

    @Test
    fun `hasMissingHealthPermissions returns false when required is zero`() {
        assertFalse(hasMissingHealthPermissions(requiredCount = 0, grantedCount = 0))
    }

    @Test
    fun `hasMissingHealthPermissions returns true when none granted but some required`() {
        assertTrue(hasMissingHealthPermissions(requiredCount = 2, grantedCount = 0))
    }

    // ── canGrantHealthPermissions ──

    @Test
    fun `canGrantHealthPermissions returns true when available and missing`() {
        val result = canGrantHealthPermissions(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 1
        )
        assertTrue(result)
    }

    @Test
    fun `canGrantHealthPermissions returns false when not available`() {
        val result = canGrantHealthPermissions(
            healthState = HealthConnectUiState.NotInstalled,
            requiredCount = 2,
            grantedCount = 1
        )
        assertFalse(result)
    }

    @Test
    fun `canGrantHealthPermissions returns false when all granted`() {
        val result = canGrantHealthPermissions(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2
        )
        assertFalse(result)
    }

    @Test
    fun `canGrantHealthPermissions returns false when update required`() {
        val result = canGrantHealthPermissions(
            healthState = HealthConnectUiState.UpdateRequired,
            requiredCount = 2,
            grantedCount = 0
        )
        assertFalse(result)
    }

    // ── canRefreshDashboard ──

    @Test
    fun `canRefreshDashboard returns true when available`() {
        assertTrue(canRefreshDashboard(HealthConnectUiState.Available))
    }

    @Test
    fun `canRefreshDashboard returns false when not installed`() {
        assertFalse(canRefreshDashboard(HealthConnectUiState.NotInstalled))
    }

    @Test
    fun `canRefreshDashboard returns false when not supported`() {
        assertFalse(canRefreshDashboard(HealthConnectUiState.NotSupported))
    }

    @Test
    fun `canRefreshDashboard returns false when update required`() {
        assertFalse(canRefreshDashboard(HealthConnectUiState.UpdateRequired))
    }

    @Test
    fun `canRefreshDashboard returns false when unknown`() {
        assertFalse(canRefreshDashboard(HealthConnectUiState.Unknown))
    }
}
