package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    ScreenContainer(title = "") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(700.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                LifeFlowSignalPill(text = "Protected access")

                Spacer(modifier = Modifier.height(22.dp))

                ProtectedAccessLoginCard(
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
    }
}
