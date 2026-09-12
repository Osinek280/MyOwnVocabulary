package com.example.myownvocabulary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun lightScheme(): ColorScheme = lightColorScheme(
    primary = LightPalette.primary,
    onPrimary = LightPalette.onPrimary,
    primaryContainer = LightPalette.primaryContainer,
    onPrimaryContainer = LightPalette.onPrimaryContainer,
    secondary = LightPalette.secondary,
    onSecondary = LightPalette.onSecondary,
    secondaryContainer = LightPalette.secondaryContainer,
    onSecondaryContainer = LightPalette.onSecondaryContainer,
    tertiary = LightPalette.tertiary,
    onTertiary = LightPalette.onTertiary,
    tertiaryContainer = LightPalette.tertiaryContainer,
    onTertiaryContainer = LightPalette.onTertiaryContainer,
    error = LightPalette.error,
    onError = LightPalette.onError,
    errorContainer = LightPalette.errorContainer,
    onErrorContainer = LightPalette.onErrorContainer,
    background = LightPalette.background,
    onBackground = LightPalette.onBackground,
    surface = LightPalette.surface,
    onSurface = LightPalette.onSurface,
    surfaceVariant = LightPalette.surfaceVariant,
    onSurfaceVariant = LightPalette.onSurfaceVariant,
    outline = LightPalette.outline,
    outlineVariant = LightPalette.outlineVariant,
    inverseSurface = LightPalette.inverseSurface,
    inverseOnSurface = LightPalette.inverseOnSurface,
    inversePrimary = LightPalette.inversePrimary,
    scrim = LightPalette.scrim,
    surfaceTint = LightPalette.primary,
    surfaceDim = LightPalette.surfaceDim,
    surfaceBright = LightPalette.surfaceBright,
    surfaceContainerLowest = LightPalette.surfaceContainerLowest,
    surfaceContainerLow = LightPalette.surfaceContainerLow,
    surfaceContainer = LightPalette.surfaceContainer,
    surfaceContainerHigh = LightPalette.surfaceContainerHigh,
    surfaceContainerHighest = LightPalette.surfaceContainerHighest,
)

private fun darkScheme(): ColorScheme = darkColorScheme(
    primary = DarkPalette.primary,
    onPrimary = DarkPalette.onPrimary,
    primaryContainer = DarkPalette.primaryContainer,
    onPrimaryContainer = DarkPalette.onPrimaryContainer,
    secondary = DarkPalette.secondary,
    onSecondary = DarkPalette.onSecondary,
    secondaryContainer = DarkPalette.secondaryContainer,
    onSecondaryContainer = DarkPalette.onSecondaryContainer,
    tertiary = DarkPalette.tertiary,
    onTertiary = DarkPalette.onTertiary,
    tertiaryContainer = DarkPalette.tertiaryContainer,
    onTertiaryContainer = DarkPalette.onTertiaryContainer,
    error = DarkPalette.error,
    onError = DarkPalette.onError,
    errorContainer = DarkPalette.errorContainer,
    onErrorContainer = DarkPalette.onErrorContainer,
    background = DarkPalette.background,
    onBackground = DarkPalette.onBackground,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.onSurface,
    surfaceVariant = DarkPalette.surfaceVariant,
    onSurfaceVariant = DarkPalette.onSurfaceVariant,
    outline = DarkPalette.outline,
    outlineVariant = DarkPalette.outlineVariant,
    inverseSurface = DarkPalette.inverseSurface,
    inverseOnSurface = DarkPalette.inverseOnSurface,
    inversePrimary = DarkPalette.inversePrimary,
    scrim = DarkPalette.scrim,
    surfaceTint = DarkPalette.primary,
    surfaceDim = DarkPalette.surfaceDim,
    surfaceBright = DarkPalette.surfaceBright,
    surfaceContainerLowest = DarkPalette.surfaceContainerLowest,
    surfaceContainerLow = DarkPalette.surfaceContainerLow,
    surfaceContainer = DarkPalette.surfaceContainer,
    surfaceContainerHigh = DarkPalette.surfaceContainerHigh,
    surfaceContainerHighest = DarkPalette.surfaceContainerHighest,
)

@Composable
fun MyOwnVocabularyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkScheme() else lightScheme()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
