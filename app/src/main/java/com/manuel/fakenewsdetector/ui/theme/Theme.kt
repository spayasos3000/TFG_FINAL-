package com.manuel.fakenewsdetector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    primary = DarkPurplePrimary,
    onPrimary = DarkPurpleOnPrimary,
    primaryContainer = DarkPurpleContainer,
    onPrimaryContainer = DarkPurpleOnContainer,
    secondary = DarkPurpleSecondary,
    onSecondary = DarkPurpleOnSecondary,
    secondaryContainer = DarkPurpleSecondaryContainer,
    onSecondaryContainer = DarkPurpleOnSecondaryContainer,
    tertiary = VerdictSuccess,
    onTertiary = Color.White,
    tertiaryContainer = VerdictSuccessPale,
    onTertiaryContainer = VerdictSuccess,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = VerdictDanger,
    onError = Color.White,
    errorContainer = VerdictDangerPale,
    onErrorContainer = VerdictDanger
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
