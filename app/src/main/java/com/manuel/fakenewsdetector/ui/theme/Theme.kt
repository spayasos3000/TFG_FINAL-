package com.manuel.fakenewsdetector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = NeutralSurface,
    primaryContainer = BrandBluePale,
    onPrimaryContainer = NavyBlue,
    secondary = NavyBlue,
    onSecondary = NeutralSurface,
    secondaryContainer = BrandBluePale,
    onSecondaryContainer = NavyBlue,
    tertiary = VerdictSuccess,
    onTertiary = NeutralSurface,
    tertiaryContainer = VerdictSuccessPale,
    onTertiaryContainer = VerdictSuccess,
    background = NeutralBackground,
    onBackground = TextHeading,
    surface = NeutralSurface,
    onSurface = TextHeading,
    surfaceVariant = NeutralBackground,
    onSurfaceVariant = TextBody,
    outline = NeutralBorder,
    error = VerdictDanger,
    onError = NeutralSurface,
    errorContainer = VerdictDangerPale,
    onErrorContainer = VerdictDanger
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBluePale,
    onPrimary = NavyBlue,
    primaryContainer = NavyBlue,
    onPrimaryContainer = BrandBluePale,
    secondary = BrandBluePale,
    onSecondary = NavyBlue,
    secondaryContainer = NavyBlue,
    onSecondaryContainer = BrandBluePale,
    tertiary = VerdictSuccessPale,
    onTertiary = VerdictSuccess,
    tertiaryContainer = VerdictSuccess,
    onTertiaryContainer = VerdictSuccessPale,
    background = NavyBlue,
    onBackground = NeutralBackground,
    surface = TextHeading,
    onSurface = NeutralSurface,
    surfaceVariant = TextBody,
    onSurfaceVariant = NeutralBackground,
    outline = TextMuted,
    error = VerdictDangerPale,
    onError = VerdictDanger,
    errorContainer = VerdictDanger,
    onErrorContainer = VerdictDangerPale
)

@Composable
fun FakeNewsDetectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
