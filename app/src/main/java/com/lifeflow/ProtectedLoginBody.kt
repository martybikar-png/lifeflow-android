package com.lifeflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PremiumLoginBody(
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
            .padding(start = 18.dp, end = 18.dp, top = 112.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unlock LifeFlow",
            color = PremiumLoginTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Private. Device-bound. Verified.",
            color = PremiumLoginTextSecondary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp)
        )

        PremiumLoginMethodGrid(
            selectedMethod = selectedMethod,
            onSelectMethod = onSelectMethod,
            modifier = Modifier.padding(top = PremiumLoginMethodGridTopGap)
        )

        PremiumLoginActionArea(
            isAuthenticating = isAuthenticating,
            accessStatus = accessStatus,
            onReviewAccess = onReviewAccess,
            onAuthenticate = onAuthenticate
        )
    }
}

@Composable
private fun PremiumLoginMethodGrid(
    selectedMethod: LoginMethod,
    onSelectMethod: (LoginMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PremiumLoginMethodRowGap)
    ) {
        PremiumLoginMethodOptions
            .chunked(PremiumLoginMethodGridColumns)
            .forEach { rowOptions ->
                PremiumLoginMethodGridRow(
                    options = rowOptions,
                    selectedMethod = selectedMethod,
                    onSelectMethod = onSelectMethod
                )
            }
    }
}

@Composable
private fun PremiumLoginMethodGridRow(
    options: List<PremiumLoginMethodOption>,
    selectedMethod: LoginMethod,
    onSelectMethod: (LoginMethod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = PremiumLoginMethodColumnGap,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        options.forEach { option ->
            PremiumLoginMethodRow(
                title = option.title,
                subtitle = option.subtitle,
                iconResId = option.iconResId,
                selected = selectedMethod == option.method,
                onClick = { onSelectMethod(option.method) },
                modifier = Modifier.width(PremiumLoginMethodButtonWidth)
            )
        }
    }
}

@Composable
private fun ColumnScope.PremiumLoginActionArea(
    isAuthenticating: Boolean,
    accessStatus: String,
    onReviewAccess: () -> Unit,
    onAuthenticate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        PremiumLoginEnterButton(
            label = if (isAuthenticating) "Signing in…" else "Enter",
            subtitle = "Verified",
            iconResId = R.drawable.lf_ic_authenticate,
            enabled = !isAuthenticating,
            onClick = onAuthenticate,
            modifier = Modifier
                .width(PremiumLoginEnterButtonWidth)
                .align(Alignment.Center)
                .offset(y = LifeFlowLoginEnterBaselineOffset)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
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
                text = "Review trust",
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

private data class PremiumLoginMethodOption(
    val method: LoginMethod,
    val title: String,
    val subtitle: String,
    val iconResId: Int
)

private val PremiumLoginMethodOptions = listOf(
    PremiumLoginMethodOption(
        method = LoginMethod.BIOMETRIC_ID,
        title = "Biometric",
        subtitle = "Strong check",
        iconResId = R.drawable.lf_ic_authenticate
    ),
    PremiumLoginMethodOption(
        method = LoginMethod.SECURE_PROMPT,
        title = "Secure prompt",
        subtitle = "Guarded flow",
        iconResId = R.drawable.lf_ic_authenticate
    ),
    PremiumLoginMethodOption(
        method = LoginMethod.DEVICE_BOUND,
        title = "Device bound",
        subtitle = "This device",
        iconResId = R.drawable.lf_ic_authenticate
    ),
    PremiumLoginMethodOption(
        method = LoginMethod.LOCAL_VAULT,
        title = "Local vault",
        subtitle = "Encrypted vault",
        iconResId = R.drawable.lf_ic_permissions
    )
)
