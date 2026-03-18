package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun RecoveryActionsContent(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onResetVault: () -> Unit,
    showAuthenticateAction: Boolean,
    showResetVaultAction: Boolean
) {
    if (showAuthenticateAction) {
        ErrorAuthenticateButton(onAuthenticate = onAuthenticate)
        Spacer(modifier = Modifier.height(8.dp))
    }

    ErrorHealthAccessButton(
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount,
        onGrantHealthPermissions = onGrantHealthPermissions
    )

    Spacer(modifier = Modifier.height(8.dp))

    ErrorOpenHealthConnectSettingsButton(
        onOpenHealthConnectSettings = onOpenHealthConnectSettings
    )

    if (showResetVaultAction) {
        Spacer(modifier = Modifier.height(8.dp))
        ErrorResetVaultButton(onResetVault = onResetVault)
    }
}

@Composable
private fun ErrorAuthenticateButton(
    onAuthenticate: () -> Unit
) {
    Button(
        onClick = onAuthenticate,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Authenticate again")
    }
}

@Composable
private fun ErrorHealthAccessButton(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onGrantHealthPermissions: () -> Unit
) {
    val canReviewHealthAccess = canGrantHealthPermissions(
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    OutlinedButton(
        onClick = onGrantHealthPermissions,
        enabled = canReviewHealthAccess,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (canReviewHealthAccess) {
                "Review Health access"
            } else {
                "Health access ready"
            }
        )
    }
}

@Composable
private fun ErrorOpenHealthConnectSettingsButton(
    onOpenHealthConnectSettings: () -> Unit
) {
    OutlinedButton(
        onClick = onOpenHealthConnectSettings,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Open Health Connect settings")
    }
}

@Composable
private fun ErrorResetVaultButton(
    onResetVault: () -> Unit
) {
    OutlinedButton(
        onClick = onResetVault,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Reset vault")
    }
}
