package com.lifeflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LifeFlowColorScheme = lightColorScheme(
    primary = TrustIndigo,
    onPrimary = TextInverse,
    primaryContainer = TrustLavender,
    onPrimaryContainer = TextPrimary,
    secondary = InsightBlue,
    onSecondary = TextInverse,
    secondaryContainer = InsightGlow,
    onSecondaryContainer = TextPrimary,
    tertiary = WellbeingTeal,
    onTertiary = TextInverse,
    tertiaryContainer = WellbeingMint,
    onTertiaryContainer = TextPrimary,
    background = ShellMistWhite,
    onBackground = TextPrimary,
    surface = ShellQuietSurface,
    onSurface = TextPrimary,
    surfaceVariant = ShellFrostSurface,
    onSurfaceVariant = TextSecondary,
    error = RecoveryCorál,
    onError = TextInverse,
    errorContainer = RecoveryPeach,
    onErrorContainer = TextPrimary,
    outline = ShellCloudLavender
)

@Composable
fun LifeFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LifeFlowColorScheme,
        typography = Typography,
        shapes = LifeFlowShapes,
        content = content
    )
}
