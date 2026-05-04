package com.lifeflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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

        PremiumLoginMethodGrid(
            selectedMethod = selectedMethod,
            onSelectMethod = onSelectMethod
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
    onSelectMethod: (LoginMethod) -> Unit
) {
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
