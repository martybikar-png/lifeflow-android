package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val SplashLogoSize = 196.dp
private const val SplashMaskOpaqueStop = 0.80f
private const val SplashMaskSoftStop = 0.96f
private const val SplashMaskSoftAlpha = 0.14f
private const val SplashMaskRadiusMultiplier = 0.66f

@Composable
internal fun LifeFlowSplashLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = SplashLogoSize
) {
    Box(
        modifier = modifier
            .size(logoSize)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                val glow = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFFCFCFC),
                        0.72f to Color(0xFFFCFCFC),
                        0.92f to Color(0xFFFCFCFC).copy(alpha = 0.72f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(
                        x = size.width / 2f,
                        y = size.height / 2f
                    ),
                    radius = size.minDimension * 0.82f
                )
                val mask = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White,
                        SplashMaskOpaqueStop to Color.White,
                        SplashMaskSoftStop to Color.White.copy(alpha = SplashMaskSoftAlpha),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(
                        x = size.width / 2f,
                        y = size.height / 2f
                    ),
                    radius = size.minDimension * SplashMaskRadiusMultiplier
                )

                onDrawWithContent {
                    drawRect(brush = glow)
                    drawContent()
                    drawRect(
                        brush = mask,
                        blendMode = BlendMode.DstIn
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.lifeflow_splash_icon),
            contentDescription = "LifeFlow app icon",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
