package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val lastUpdatedFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd HH:mm:ss",
    Locale.getDefault()
)

internal fun healthStateLabel(state: HealthConnectUiState): String {
    return when (state) {
        HealthConnectUiState.Unknown -> "Unknown"
        HealthConnectUiState.Available -> "Available"
        HealthConnectUiState.NotInstalled -> "Not installed"
        HealthConnectUiState.NotSupported -> "Not supported"
        HealthConnectUiState.UpdateRequired -> "Update required"
    }
}

internal fun availabilityLabel(availability: DigitalTwinState.Availability): String {
    return when (availability) {
        DigitalTwinState.Availability.OK -> "OK"
        DigitalTwinState.Availability.NO_DATA -> "No data"
        DigitalTwinState.Availability.PERMISSION_DENIED -> "Permission denied"
        DigitalTwinState.Availability.UNKNOWN -> "Unknown"
        DigitalTwinState.Availability.BLOCKED -> "Blocked"
    }
}

internal fun yesNoLabel(value: Boolean): String {
    return if (value) "Yes" else "No"
}

internal fun formatLastUpdated(epochMillis: Long): String {
    if (epochMillis <= 0L) {
        return "Not available"
    }

    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(lastUpdatedFormatter)
}