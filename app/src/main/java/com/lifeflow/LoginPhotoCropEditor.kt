package com.lifeflow

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LoginPhotoCropTransform(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val previewSizePx: Int
)

@Composable
internal fun LoginPhotoCropEditor(
    sourceUri: Uri,
    isSaving: Boolean,
    onConfirm: (LoginPhotoCropTransform) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    var imageBitmap by remember(sourceUri) {
        mutableStateOf<ImageBitmap?>(null)
    }
    var scale by remember(sourceUri) {
        mutableFloatStateOf(1f)
    }
    var offset by remember(sourceUri) {
        mutableStateOf(Offset.Zero)
    }
    var previewSizePx by remember(sourceUri) {
        mutableIntStateOf(1)
    }

    LaunchedEffect(sourceUri) {
        imageBitmap = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
        offset += panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PremiumLoginBlueTop, PremiumLoginBlueBottom)
                )
            )
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Adjust photo",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pinch to zoom. Move to position.",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(286.dp)
                    .onSizeChanged { previewSizePx = it.width.coerceAtLeast(1) }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(2.dp, PremiumLoginGoldCircleBrush, CircleShape)
                    .transformable(transformState),
                contentAlignment = Alignment.Center
            ) {
                val loadedImage = imageBitmap
                if (loadedImage != null) {
                    Image(
                        bitmap = loadedImage,
                        contentDescription = "Photo crop preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            LifeFlowPrimaryActionButton(
                label = if (isSaving) "Saving…" else "Use photo",
                onClick = {
                    onConfirm(
                        LoginPhotoCropTransform(
                            scale = scale,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            previewSizePx = previewSizePx
                        )
                    )
                },
                enabled = !isSaving && imageBitmap != null
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Cancel",
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(horizontal = 22.dp, vertical = 10.dp)
                    .background(Color.Transparent)
                    .clickable(enabled = !isSaving, onClick = onCancel)
            )
        }
    }
}

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 4f
