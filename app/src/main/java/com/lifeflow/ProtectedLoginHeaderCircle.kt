package com.lifeflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PremiumLoginTopPanel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 40.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Welcome back",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Protected log in",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
            )
        }
    }
}

@Composable
internal fun PremiumCenterCircle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(166.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp)
                .size(152.dp)
                .premiumLoginPhotoWhiteTopLevitationShadow(CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 18.dp)
                .size(154.dp)
                .premiumLoginPhotoBlueLevitationShadow(CircleShape)
        )

        Box(
            modifier = Modifier
                .size(146.dp)
                .premiumLoginFloatingPhotoSurface(CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ProtectedBiometricAccessIcon(
                modifier = Modifier.size(38.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-22).dp, y = (-12).dp)
                .size(42.dp)
                .premiumLoginPlusWhiteTopLevitationShadow(CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-22).dp, y = 4.dp)
                .size(42.dp)
                .premiumLoginPlusBlueLevitationShadow(CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-22).dp, y = (-8).dp)
                .size(34.dp)
                .premiumLoginAddButtonSurface(CircleShape)
                .clip(CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = PremiumLoginLink,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
