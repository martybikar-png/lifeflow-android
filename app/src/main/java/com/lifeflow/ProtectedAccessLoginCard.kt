package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun ProtectedAccessLoginCard(
    modifier: Modifier = Modifier,
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    var selectedMethod by rememberSaveable {
        mutableStateOf(LoginMethod.FACE_ID)
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
                    colors = listOf(
                        PremiumLoginBlueTop,
                        PremiumLoginBlueBottom
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

        PremiumCenterCircle(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = whiteStart - PremiumLoginCenterCircleLift)
        )
    }
}

@Composable
private fun PremiumLoginBody(
    modifier: Modifier = Modifier,
    selectedMethod: LoginMethod,
    onSelectMethod: (LoginMethod) -> Unit,
    isAuthenticating: Boolean,
    accessStatus: String,
    onReviewAccess: () -> Unit,
    onAuthenticate: () -> Unit
) {
    Column(
        modifier = modifier
            .premiumLoginBodyCardSurface(PremiumLoginBodyShape)
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 108.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log in",
            color = PremiumLoginTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PremiumLoginMethodRow(
                title = "Face ID",
                subtitle = "Face recognition",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.FACE_ID,
                onClick = { onSelectMethod(LoginMethod.FACE_ID) },
                modifier = Modifier.weight(1f)
            )

            PremiumLoginMethodRow(
                title = "Iris ID",
                subtitle = "Iris recognition",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.IRIS_ID,
                onClick = { onSelectMethod(LoginMethod.IRIS_ID) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PremiumLoginMethodRow(
                title = "Fingerprint",
                subtitle = "Fingerprint login",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.FINGERPRINT,
                onClick = { onSelectMethod(LoginMethod.FINGERPRINT) },
                modifier = Modifier.weight(1f)
            )

            PremiumLoginMethodRow(
                title = "Device ID",
                subtitle = "Local access",
                iconResId = R.drawable.lf_ic_permissions,
                selected = selectedMethod == LoginMethod.DEVICE_BOUND,
                onClick = { onSelectMethod(LoginMethod.DEVICE_BOUND) },
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LifeFlowPrimaryActionButton(
                label = if (isAuthenticating) "Signing in…" else "Enter",
                onClick = onAuthenticate,
                enabled = !isAuthenticating,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                iconResId = R.drawable.lf_ic_authenticate
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
                    .offset(y = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accessStatus,
                    color = PremiumLoginTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 9.sp,
                        lineHeight = 12.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Review access",
                    color = PremiumLoginTextPrimary,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable(onClick = onReviewAccess)
                )
            }
        }
    }
}
