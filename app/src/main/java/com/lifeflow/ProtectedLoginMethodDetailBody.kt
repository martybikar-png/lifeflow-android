package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
internal fun PremiumLoginMethodDetailBody(
    modifier: Modifier = Modifier,
    method: LoginMethod,
    onBack: () -> Unit
) {
    val content = method.detailContent()

    Column(
        modifier = modifier
            .premiumLoginBodyCardSurface(PremiumLoginBodyShape)
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 112.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = content.title,
            color = PremiumLoginTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = content.subtitle,
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

        Text(
            text = content.body,
            color = PremiumLoginTextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LifeFlowHomeSecondaryActionButton(
                label = "Back",
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = LifeFlowLoginEnterBaselineOffset)
            )
        }
    }
}

private data class LoginMethodDetailContent(
    val title: String,
    val subtitle: String,
    val body: String
)

private fun LoginMethod.detailContent(): LoginMethodDetailContent {
    return when (this) {
        LoginMethod.BIOMETRIC_ID -> LoginMethodDetailContent(
            title = "Biometric",
            subtitle = "Strong ID.",
            body = "Unlock with a strong biometric check\nbound to this secure session."
        )

        LoginMethod.SECURE_PROMPT -> LoginMethodDetailContent(
            title = "Prompt",
            subtitle = "Protected UI.",
            body = "A guarded login prompt keeps the protected\nentry flow clear and controlled."
        )

        LoginMethod.DEVICE_BOUND -> LoginMethodDetailContent(
            title = "Device bound",
            subtitle = "This phone only.",
            body = "Protected access is tied to this device\nso the session cannot move silently."
        )

        LoginMethod.LOCAL_VAULT -> LoginMethodDetailContent(
            title = "Local vault",
            subtitle = "Encrypted.",
            body = "Sensitive local data stays protected\ninside the encrypted LifeFlow vault."
        )
    }
}
