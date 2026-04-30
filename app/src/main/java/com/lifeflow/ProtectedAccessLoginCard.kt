package com.lifeflow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

        PremiumLoginGoldDivider(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = whiteStart)
                .fillMaxWidth()
        )

        PremiumCenterCircle(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = whiteStart - PremiumLoginCenterCircleLift)
        )
    }
}


@Composable
private fun PremiumLoginGoldDivider(
    modifier: Modifier = Modifier
) {
    val shine = rememberInfiniteTransition(label = "dividerShine").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9750, easing = LinearEasing), RepeatMode.Restart),
        label = "dividerShineProgress"
    ).value
    Canvas(
        modifier = modifier.height(48.dp)
    ) {
        val strokeWidth = 3.dp.toPx()
        val radius = 44.dp.toPx()
        val curve = 0.55228475f
        val cornerControl = radius * curve

        val path = Path().apply {
            moveTo(-2f, radius)
            cubicTo(
                -2f,
                radius - cornerControl,
                radius - cornerControl - 2f,
                0f,
                radius - 2f,
                0f
            )
            lineTo(size.width - radius - 2f, 0f)
            cubicTo(
                size.width - radius + cornerControl - 2f,
                0f,
                size.width - 2f,
                radius - cornerControl,
                size.width - 2f,
                radius
            )
        }

        drawPath(
            path = path,
            brush = PremiumLoginGoldDividerBrush,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )

        val measure = PathMeasure(); measure.setPath(path, false)
        val total = measure.length
        val start = total * shine
        val length = total * 0.14f
        val alphas = listOf(0.03f,0.06f,0.11f,0.18f,0.30f,0.48f,0.86f,0.48f,0.30f,0.18f,0.11f,0.06f,0.03f)
        alphas.forEachIndexed { index, alpha ->
            val from = start + length * index / alphas.size
            val to = start + length * (index + 1) / alphas.size
            val segment = Path()
            if (measure.getSegment(from.coerceAtMost(total), to.coerceAtMost(total), segment, true)) {
                drawPath(
                    path = segment,
                    color = Color(0xFFFFF6D8).copy(alpha = alpha * 0.24f),
                    style = Stroke(
                        width = strokeWidth + 6.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
                drawPath(
                    path = segment,
                    color = Color(0xFFFFF1B0).copy(alpha = alpha * 0.55f),
                    style = Stroke(
                        width = strokeWidth + 3.0.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
                drawPath(
                    path = segment,
                    color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.78f),
                    style = Stroke(
                        width = strokeWidth + 0.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
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
            .padding(start = 18.dp, end = 18.dp, top = 112.dp, bottom = 18.dp),
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
                title = "Biometric ID",
                subtitle = "Strong biometric",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.BIOMETRIC_ID,
                onClick = { onSelectMethod(LoginMethod.BIOMETRIC_ID) },
                modifier = Modifier.weight(1f)
            )

            PremiumLoginMethodRow(
                title = "Secure prompt",
                subtitle = "Android protected",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.SECURE_PROMPT,
                onClick = { onSelectMethod(LoginMethod.SECURE_PROMPT) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PremiumLoginMethodRow(
                title = "Device bound",
                subtitle = "This device only",
                iconResId = R.drawable.lf_ic_authenticate,
                selected = selectedMethod == LoginMethod.LOCAL_VAULT,
                onClick = { onSelectMethod(LoginMethod.LOCAL_VAULT) },
                modifier = Modifier.weight(1f)
            )

            PremiumLoginMethodRow(
                title = "Local vault",
                subtitle = "Encrypted access",
                iconResId = R.drawable.lf_ic_permissions,
                selected = selectedMethod == LoginMethod.LOCAL_VAULT,
                onClick = { onSelectMethod(LoginMethod.LOCAL_VAULT) },
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
                    .align(Alignment.Center)
                    .offset(y = LifeFlowLoginEnterBaselineOffset),
                iconResId = R.drawable.lf_ic_authenticate
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
                    .offset(y = 10.dp),
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
