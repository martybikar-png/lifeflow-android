package com.lifeflow

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun PremiumLoginTopPanel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 60.dp, bottom = 16.dp)
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
    modifier: Modifier = Modifier,
    loginPhotoVersion: Long = 0L,
    onPickLoginPhoto: () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "premiumCircleShine")
    val shineAngle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "premiumCircleShineAngle"
    ).value

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
                .border(
                    width = 1.6.dp,
                    brush = PremiumLoginGoldCircleBrush,
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PremiumLoginCirclePhoto(loginPhotoVersion = loginPhotoVersion)
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(146.dp)
        ) {
            val stroke = 1.8.dp.toPx()
            val edgeSweep = 7.0f
            val coreSweep = 7.0f
            drawArc(
                color = Color(0xFFD4AF37).copy(alpha = 0.38f),
                startAngle = shineAngle,
                sweepAngle = edgeSweep,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFFFF1B0).copy(alpha = 0.98f),
                startAngle = shineAngle + edgeSweep,
                sweepAngle = coreSweep,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFD4AF37).copy(alpha = 0.38f),
                startAngle = shineAngle + edgeSweep + coreSweep,
                sweepAngle = edgeSweep,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
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
                .clickable(onClick = onPickLoginPhoto),
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

@Composable
private fun PremiumLoginCirclePhoto(
    loginPhotoVersion: Long
) {
    val context = LocalContext.current.applicationContext
    val store = remember(context) {
        LoginPhotoStore(context)
    }
    var selectedPhoto by remember(loginPhotoVersion) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(loginPhotoVersion) {
        selectedPhoto = withContext(Dispatchers.IO) {
            store.loginPhotoFileOrNull()
                ?.let { BitmapFactory.decodeFile(it.absolutePath) }
                ?.asImageBitmap()
        }
    }

    val photo = selectedPhoto
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = "Selected login photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.lifeflow_core_in_hands_softer),
            contentDescription = "LifeFlow protected access fallback photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
