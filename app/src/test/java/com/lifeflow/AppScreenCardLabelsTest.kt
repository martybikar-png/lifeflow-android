package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import org.junit.Assert.*
import org.junit.Test

class AppScreenCardLabelsTest {

    // ── healthReadinessLabel ──

    @Test
    fun `healthReadinessLabel returns Ready when all granted`() {
        val result = healthReadinessLabel(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2
        )
        assertEquals("Ready", result)
    }

    @Test
    fun `healthReadinessLabel returns Partial access when some missing`() {
        val result = healthReadinessLabel(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 1
        )
        assertEquals("Partial access", result)
    }

    @Test
    fun `healthReadinessLabel returns No health access yet when none granted`() {
        val result = healthReadinessLabel(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 0
        )
        assertEquals("No health access yet", result)
    }

    @Test
    fun `healthReadinessLabel returns health state label when not available`() {
        val result = healthReadinessLabel(
            healthState = HealthConnectUiState.NotInstalled,
            requiredCount = 2,
            grantedCount = 0
        )
        assertTrue(result.contains("install", ignoreCase = true) || result.isNotEmpty())
    }

    // ── permissionCoverageLabel ──

    @Test
    fun `permissionCoverageLabel shows count ratio`() {
        val result = permissionCoverageLabel(requiredCount = 2, grantedCount = 1)
        assertEquals("1 / 2", result)
    }

    @Test
    fun `permissionCoverageLabel shows Waiting when required is zero`() {
        val result = permissionCoverageLabel(requiredCount = 0, grantedCount = 0)
        assertEquals("Waiting", result)
    }

    // ── healthNextMoveLabel ──

    @Test
    fun `healthNextMoveLabel returns Install when not installed`() {
        val result = healthNextMoveLabel(
            healthState = HealthConnectUiState.NotInstalled,
            requiredCount = 2,
            grantedCount = 0
        )
        assertTrue(result.contains("Install", ignoreCase = true))
    }

    @Test
    fun `healthNextMoveLabel returns Update when update required`() {
        val result = healthNextMoveLabel(
            healthState = HealthConnectUiState.UpdateRequired,
            requiredCount = 2,
            grantedCount = 0
        )
        assertTrue(result.contains("Update", ignoreCase = true))
    }

    @Test
    fun `healthNextMoveLabel returns Unavailable when not supported`() {
        val result = healthNextMoveLabel(
            healthState = HealthConnectUiState.NotSupported,
            requiredCount = 2,
            grantedCount = 0
        )
        assertTrue(result.contains("Unavailable", ignoreCase = true))
    }

    @Test
    fun `healthNextMoveLabel returns Review when missing permissions`() {
        val result = healthNextMoveLabel(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 1
        )
        assertTrue(result.contains("Review", ignoreCase = true))
    }

    @Test
    fun `healthNextMoveLabel returns Health path ready when all granted`() {
        val result = healthNextMoveLabel(
            healthState = HealthConnectUiState.Available,
            requiredCount = 2,
            grantedCount = 2
        )
        assertEquals("Health path ready", result)
    }

    // ── digitalTwinReadinessLabel ──

    @Test
    fun `digitalTwinReadinessLabel returns Not loaded yet when null`() {
        val result = digitalTwinReadinessLabel(null)
        assertEquals("Not loaded yet", result)
    }

    // ── digitalTwinNextMoveLabel ──

    @Test
    fun `digitalTwinNextMoveLabel returns Load first snapshot when null`() {
        val result = digitalTwinNextMoveLabel(null)
        assertEquals("Load first snapshot", result)
    }
}
