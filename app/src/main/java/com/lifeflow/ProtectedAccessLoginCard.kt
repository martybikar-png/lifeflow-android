package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun ProtectedAccessLoginCard(
    modifier: Modifier = Modifier,
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    loginPhotoVersion: Long,
    onPickLoginPhoto: () -> Unit,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    var selectedMethod by rememberSaveable {
        mutableStateOf(LoginMethod.BIOMETRIC_ID)
    }

    val hasMissingPermissions = hasMissingHealthPermissions(
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    val accessStatus = when {
        healthState != HealthConnectUiState.Available ->
            "Health Connect needs attention"
        hasMissingPermissions ->
            "$grantedCount of $requiredCount permissions ready"
        else ->
            "All health access ready"
    }

    val reviewAccessAction = if (healthState != HealthConnectUiState.Available) {
        onOpenHealthConnectSettings
    } else {
        onGrantHealthPermissions
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to PremiumLoginBlueTop,
                        0.15f to PremiumLoginBlueEdge,
                        0.24f to PremiumLoginBlueBottom,
                        1.0f to PremiumLoginBlueBottom
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        val whiteStart = maxHeight * PremiumLoginWhiteStartRatio

        PremiumLoginTopPanel(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(whiteStart)
        )

        PremiumLoginBody(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(top = whiteStart),
            selectedMethod = selectedMethod,
            onSelectMethod = { selectedMethod = it },
            isAuthenticating = isAuthenticating,
            accessStatus = accessStatus,
            onReviewAccess = reviewAccessAction,
            onAuthenticate = onAuthenticate
        )

        PremiumLoginGoldDivider(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = whiteStart)
                .fillMaxWidth()
        )

        PremiumCenterCircle(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = whiteStart - PremiumLoginCenterCircleLift),
            loginPhotoVersion = loginPhotoVersion,
            onPickLoginPhoto = onPickLoginPhoto
        )
    }
}
