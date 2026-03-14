package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

@Composable
internal fun GuidanceCard(
    title: String,
    message: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

internal fun loadingGuidanceMessage(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int
): String {
    return when {
        isAuthenticating ->
            "Authentication is already active. As soon as the current checks finish, the protected dashboard can continue."

        healthState == HealthConnectUiState.NotInstalled ->
            "Install Health Connect first, then return here and authenticate again."

        healthState == HealthConnectUiState.UpdateRequired ->
            "Update Health Connect first, then come back and continue with authentication."

        requiredCount > 0 && grantedCount < requiredCount ->
            "You can authenticate now, but reviewing Health access will unlock a fuller wellbeing snapshot."

        else ->
            "Authenticate to unlock the protected dashboard and load the first Digital Twin snapshot."
    }
}

internal fun dashboardGuidanceMessage(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?
): String {
    return when {
        healthState != HealthConnectUiState.Available ->
            "Open Health Connect settings first. Until Health Connect is ready, the dashboard cannot expose full wellbeing data."

        digitalTwinState == null ->
            "Tap Refresh now to load the first Digital Twin snapshot."

        requiredCount == 0 ->
            "Health access is still loading. Refresh again after Health Connect resolves the available access set."

        requiredCount > 0 && grantedCount < requiredCount ->
            "Review Health access to improve snapshot coverage and unblock missing signals."

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ||
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED ->
            "Some signals are blocked by Health access. Open Health Connect settings and review access to complete the snapshot."

        digitalTwinState.stepsAvailability == DigitalTwinState.Availability.NO_DATA &&
                digitalTwinState.heartRateAvailability == DigitalTwinState.Availability.NO_DATA ->
            "The dashboard is working, but there is not enough usable wellbeing data yet. Refresh again later after more data is collected."

        else ->
            "The dashboard is ready. Use Refresh now whenever you want a newer wellbeing snapshot."
    }
}

internal fun errorGuidanceMessage(
    message: String,
    showAuthenticateAction: Boolean,
    showResetVaultAction: Boolean
): String {
    return when {
        showResetVaultAction ->
            "This is a security-critical path. Reset the vault first, then authenticate again to rebuild a trusted state."

        message.contains("session expired", ignoreCase = true) ->
            "Your protected session expired. Authenticate again to continue."

        message.contains("security degraded", ignoreCase = true) ->
            "The trust state was downgraded. Authenticate again before protected flows continue."

        showAuthenticateAction ->
            "Try authenticating again first. If Health Connect still looks limited, open settings and retry."

        else ->
            "Review Health access in settings, then try recovery again."
    }
}
