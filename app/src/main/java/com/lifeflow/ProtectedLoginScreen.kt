package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun ProtectedLoginScreen(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PremiumLoginBlueTop,
                        PremiumLoginBlueBottom
                    )
                )
            )
    ) {
        ProtectedAccessLoginCard(
            modifier = Modifier.fillMaxSize(),
            isAuthenticating = isAuthenticating,
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )
    }
}
