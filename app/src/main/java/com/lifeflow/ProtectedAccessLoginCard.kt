package com.lifeflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun ProtectedAccessLoginCard(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    val hasMissingPermissions = hasMissingHealthPermissions(
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    val sessionValue = if (isAuthenticating) {
        "Protected session active"
    } else {
        "Go to sign in"
    }

    val readinessValue = when {
        healthState != HealthConnectUiState.Available ->
            "Health Connect needs attention"
        hasMissingPermissions ->
            "$grantedCount of $requiredCount permissions ready"
        else ->
            "All health access ready"
    }

    val utilityLabel = if (healthState != HealthConnectUiState.Available) {
        "Open settings"
    } else {
        "Review access"
    }

    val utilityAction = if (healthState != HealthConnectUiState.Available) {
        onOpenHealthConnectSettings
    } else {
        onGrantHealthPermissions
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .lifeFlowRaisedPanelChrome(ProtectedLoginIconShape),
            contentAlignment = Alignment.Center
        ) {
            ProtectedBiometricAccessIcon(
                modifier = Modifier.size(30.dp)
            )
        }

        ProtectedLoginHeader()

        Spacer(modifier = Modifier.height(6.dp))

        ProtectedLoginInfoRow(
            label = "Biometric sign in",
            value = sessionValue,
            iconResId = R.drawable.lf_ic_authenticate,
            useBiometricIcon = true
        )

        ProtectedLoginInfoRow(
            label = "Health readiness",
            value = readinessValue,
            iconResId = R.drawable.lf_ic_permissions,
            useBiometricIcon = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProtectedLoginUtilityRow(
            label = utilityLabel,
            onClick = utilityAction
        )

        Spacer(modifier = Modifier.height(28.dp))

        ProtectedLoginActions(
            isAuthenticating = isAuthenticating,
            hasMissingPermissions = hasMissingPermissions,
            healthState = healthState,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ProtectedLoginHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Protected login",
            color = ProtectedLoginCardTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Text(
            text = "Face ID, iris ID, and fingerprint ID keep your\nLifeFlow access calm and private.",
            color = ProtectedLoginCardSubtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        )
    }
}

@Composable
private fun ProtectedLoginUtilityRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Device-bound access only",
            color = ProtectedLoginOption,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 9.sp,
                lineHeight = 11.sp
            )
        )

        Text(
            text = label,
            color = ProtectedLoginAccent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}

@Composable
private fun ProtectedLoginActions(
    isAuthenticating: Boolean,
    hasMissingPermissions: Boolean,
    healthState: HealthConnectUiState,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LifeFlowPrimaryActionButton(
            label = if (isAuthenticating) {
                "Protected session active"
            } else {
                "Authenticate securely"
            },
            onClick = onAuthenticate,
            enabled = !isAuthenticating,
            iconResId = R.drawable.lf_ic_authenticate
        )

        if (hasMissingPermissions) {
            LifeFlowSecondaryActionButton(
                label = "Review health access",
                onClick = onGrantHealthPermissions,
                iconResId = R.drawable.lf_ic_permissions
            )
        }

        if (healthState != HealthConnectUiState.Available) {
            LifeFlowSecondaryActionButton(
                label = "Open Health Connect settings",
                onClick = onOpenHealthConnectSettings,
                iconResId = R.drawable.lf_ic_settings
            )
        }
    }
}