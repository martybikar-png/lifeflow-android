package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.BuildConfig
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

private val ScreenSurfaceGap = 12.dp
private val ActionVerticalGap = 8.dp

@Composable
internal fun LoadingActionsCard(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    ActionCard(
        title = "Recommended actions",
        leadingIconResId = R.drawable.lf_ic_recommended_actions
    ) {
        Text(
            text = loadingActionHint(
                isAuthenticating = isAuthenticating,
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        ActionSectionSpacer()

        ActionButtonsColumn {
            AuthenticateButton(
                isAuthenticating = isAuthenticating,
                onAuthenticate = onAuthenticate
            )

            HealthPermissionsButton(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                onGrantHealthPermissions = onGrantHealthPermissions
            )

            OpenHealthConnectSettingsButton(
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )
        }
    }
}

@Composable
internal fun DashboardActionsCard(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    digitalTwinState: DigitalTwinState?,
    onRefreshNow: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    onReAuthenticate: () -> Unit
) {
    ActionCard(title = "Dashboard actions") {
        Text(
            text = dashboardActionHint(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                digitalTwinState = digitalTwinState
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        ActionSectionSpacer()

        ActionButtonsColumn {
            RefreshDashboardButton(
                healthState = healthState,
                digitalTwinState = digitalTwinState,
                onRefreshNow = onRefreshNow
            )

            HealthPermissionsButton(
                healthState = healthState,
                requiredCount = requiredCount,
                grantedCount = grantedCount,
                onGrantHealthPermissions = onGrantHealthPermissions
            )

            OpenHealthConnectSettingsButton(
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )

            ReAuthenticateButton(
                onReAuthenticate = onReAuthenticate
            )
        }
    }
}

@Composable
internal fun ScreenFooter(
    lastAction: String,
    debugLines: List<String>
) {
    ScreenSectionSpacer()
    LastActionCard(lastAction = lastAction)

    if (BuildConfig.DEBUG) {
        ScreenSectionSpacer()
        DebugCard(debugLines = debugLines)
    }
}

@Composable
internal fun ScreenSectionSpacer() {
    Spacer(modifier = Modifier.height(ScreenSurfaceGap))
}

@Composable
private fun ActionSectionSpacer() {
    Spacer(modifier = Modifier.height(ActionVerticalGap))
}

@Composable
private fun ActionButtonsColumn(
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ActionVerticalGap)
    ) {
        content()
    }
}

@Composable
private fun AuthenticateButton(
    isAuthenticating: Boolean,
    onAuthenticate: () -> Unit
) {
    LifeFlowPrimaryActionButton(
        label = if (isAuthenticating) {
            "Protected session active"
        } else {
            "Authenticate"
        },
        onClick = onAuthenticate,
        enabled = !isAuthenticating,
        iconResId = R.drawable.lf_ic_authenticate
    )
}

@Composable
private fun RefreshDashboardButton(
    healthState: HealthConnectUiState,
    digitalTwinState: DigitalTwinState?,
    onRefreshNow: () -> Unit
) {
    LifeFlowPrimaryActionButton(
        label = if (digitalTwinState == null) {
            "Load first snapshot"
        } else {
            "Refresh now"
        },
        onClick = onRefreshNow,
        enabled = canRefreshDashboard(healthState),
        iconResId = R.drawable.lf_ic_refresh
    )
}

@Composable
private fun HealthPermissionsButton(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onGrantHealthPermissions: () -> Unit
) {
    LifeFlowSecondaryActionButton(
        label = if (hasMissingHealthPermissions(requiredCount, grantedCount)) {
            "Review health access"
        } else {
            "Health access ready"
        },
        onClick = onGrantHealthPermissions,
        enabled = canGrantHealthPermissions(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount
        ),
        iconResId = R.drawable.lf_ic_permissions
    )
}

@Composable
private fun OpenHealthConnectSettingsButton(
    onOpenHealthConnectSettings: () -> Unit
) {
    LifeFlowSecondaryActionButton(
        label = "Open Health Connect settings",
        onClick = onOpenHealthConnectSettings,
        iconResId = R.drawable.lf_ic_settings
    )
}

@Composable
private fun ReAuthenticateButton(
    onReAuthenticate: () -> Unit
) {
    LifeFlowSecondaryActionButton(
        label = "Authenticate again",
        onClick = onReAuthenticate
    )
}
