package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.core.HealthConnectUiState

private val ProtectedLoginCardTitle = Color(0xFF3D4468)
private val ProtectedLoginCardSubtitle = Color(0xFF8B93A7)
private val ProtectedLoginFieldLabel = Color(0xFF9499B7)
private val ProtectedLoginFieldValue = Color(0xFF3D4468)
private val ProtectedLoginOption = Color(0xFF6C7293)
private val ProtectedLoginAccent = Color(0xFF22CDF7)

private val ProtectedLoginIconShape = RoundedCornerShape(999.dp)
private val ProtectedLoginMiniIconShape = RoundedCornerShape(12.dp)

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
                text = utilityLabel,
                color = ProtectedLoginAccent,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable(onClick = utilityAction)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

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

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ProtectedBiometricAccessIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.085f
        val accent = ProtectedLoginAccent

        val shield = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.10f)
            cubicTo(
                size.width * 0.20f, size.height * 0.16f,
                size.width * 0.14f, size.height * 0.34f,
                size.width * 0.18f, size.height * 0.58f
            )
            cubicTo(
                size.width * 0.22f, size.height * 0.76f,
                size.width * 0.36f, size.height * 0.88f,
                size.width * 0.50f, size.height * 0.94f
            )
            cubicTo(
                size.width * 0.64f, size.height * 0.88f,
                size.width * 0.78f, size.height * 0.76f,
                size.width * 0.82f, size.height * 0.58f
            )
            cubicTo(
                size.width * 0.86f, size.height * 0.34f,
                size.width * 0.80f, size.height * 0.16f,
                size.width * 0.50f, size.height * 0.10f
            )
            close()
        }

        drawPath(
            path = shield,
            color = accent,
            style = Stroke(width = stroke)
        )

        drawArc(
            color = accent,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.30f, size.height * 0.22f),
            size = Size(size.width * 0.40f, size.height * 0.34f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        drawRoundRect(
            color = accent,
            topLeft = Offset(size.width * 0.29f, size.height * 0.44f),
            size = Size(size.width * 0.42f, size.height * 0.24f),
            cornerRadius = CornerRadius(size.width * 0.06f, size.width * 0.06f)
        )

        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.40f, size.height * 0.56f),
            end = Offset(size.width * 0.47f, size.height * 0.63f),
            strokeWidth = size.minDimension * 0.07f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.47f, size.height * 0.63f),
            end = Offset(size.width * 0.61f, size.height * 0.49f),
            strokeWidth = size.minDimension * 0.07f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ProtectedLoginInfoRow(
    label: String,
    value: String,
    iconResId: Int,
    useBiometricIcon: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .lifeFlowRaisedPanelChrome(ProtectedLoginMiniIconShape),
            contentAlignment = Alignment.Center
        ) {
            if (useBiometricIcon) {
                ProtectedBiometricAccessIcon(
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    colorFilter = ColorFilter.tint(ProtectedLoginAccent)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = ProtectedLoginFieldLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                color = ProtectedLoginFieldValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
