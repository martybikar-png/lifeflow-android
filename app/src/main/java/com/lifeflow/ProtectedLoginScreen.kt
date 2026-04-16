package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

@Composable
internal fun ProtectedLoginScreen(
    lastAction: String,
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "", useBackdrop = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(700.dp)
        ) {
            ProtectedLoginBackground(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
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

@Composable
private fun ProtectedLoginBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val hazeCenter = Offset(
            x = size.width * 0.50f,
            y = size.height * 0.39f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFEAFBFF).copy(alpha = 0.28f),
                    Color(0xFFDDF8FF).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = hazeCenter,
                radius = size.minDimension * 0.44f
            ),
            radius = size.minDimension * 0.44f,
            center = hazeCenter
        )

        // horní velké orbity
        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.42f),
            startAngle = 198f,
            sweepAngle = 126f,
            useCenter = false,
            topLeft = Offset(-size.width * 1.00f, size.height * 0.09f),
            size = Size(size.width * 2.34f, size.height * 0.38f),
            style = Stroke(width = 2.35.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.32f),
            startAngle = 314f,
            sweepAngle = 138f,
            useCenter = false,
            topLeft = Offset(size.width * 0.26f, size.height * 0.05f),
            size = Size(size.width * 1.72f, size.height * 0.50f),
            style = Stroke(width = 1.95.dp.toPx())
        )

        // střed kolem karty
        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.28f),
            startAngle = 154f,
            sweepAngle = 154f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.56f, size.height * 0.30f),
            size = Size(size.width * 1.62f, size.height * 0.26f),
            style = Stroke(width = 1.45.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.24f),
            startAngle = 338f,
            sweepAngle = 148f,
            useCenter = false,
            topLeft = Offset(size.width * 0.10f, size.height * 0.26f),
            size = Size(size.width * 1.24f, size.height * 0.30f),
            style = Stroke(width = 1.26.dp.toPx())
        )

        drawArc(
            color = Color(0xFFEAFBFF).copy(alpha = 0.18f),
            startAngle = 168f,
            sweepAngle = 122f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.18f, size.height * 0.39f),
            size = Size(size.width * 0.96f, size.height * 0.16f),
            style = Stroke(width = 1.02.dp.toPx())
        )

        // spodní orbity posazené blíž ke kartě
        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.30f),
            startAngle = 18f,
            sweepAngle = 124f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.96f, size.height * 0.60f),
            size = Size(size.width * 2.26f, size.height * 0.24f),
            style = Stroke(width = 1.72.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.24f),
            startAngle = 34f,
            sweepAngle = 126f,
            useCenter = false,
            topLeft = Offset(size.width * 0.22f, size.height * 0.57f),
            size = Size(size.width * 1.58f, size.height * 0.30f),
            style = Stroke(width = 1.38.dp.toPx())
        )

        drawArc(
            color = Color(0xFFBFEFFF).copy(alpha = 0.20f),
            startAngle = 52f,
            sweepAngle = 118f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.48f, size.height * 0.55f),
            size = Size(size.width * 1.42f, size.height * 0.20f),
            style = Stroke(width = 1.16.dp.toPx())
        )

        drawArc(
            color = Color(0xFFD7F6FF).copy(alpha = 0.18f),
            startAngle = 8f,
            sweepAngle = 104f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.14f, size.height * 0.66f),
            size = Size(size.width * 0.96f, size.height * 0.12f),
            style = Stroke(width = 0.96.dp.toPx())
        )
    }
}
