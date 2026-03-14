package com.lifeflow

import com.lifeflow.core.HealthConnectUiState

internal fun hasMissingHealthPermissions(
    requiredCount: Int,
    grantedCount: Int
): Boolean = requiredCount > 0 && grantedCount < requiredCount

internal fun canGrantHealthPermissions(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): Boolean {
    return healthState == HealthConnectUiState.Available &&
            hasMissingHealthPermissions(requiredCount, grantedCount)
}

internal fun canRefreshDashboard(
    healthState: HealthConnectUiState
): Boolean = healthState == HealthConnectUiState.Available
