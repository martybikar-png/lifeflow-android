package com.lifeflow.core

/**
 * Local UI state for Health Connect.
 * Keep it stable and explicit (no hidden magic).
 */
sealed class HealthConnectUiState {
    object Unknown : HealthConnectUiState()
    object Available : HealthConnectUiState()
    object NotInstalled : HealthConnectUiState()
    object NotSupported : HealthConnectUiState()
    object UpdateRequired : HealthConnectUiState()
}