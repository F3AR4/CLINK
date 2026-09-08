package com.clink.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ClinkPink,
    onPrimary = SurfaceLight,
    primaryContainer = ClinkPinkLight.copy(alpha = 0.2f),
    onPrimaryContainer = ClinkPinkDark,
    secondary = ClinkTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = ClinkTealLight.copy(alpha = 0.2f),
    onSecondaryContainer = ClinkTealDark,
    tertiary = ClinkNavy,
    onTertiary = SurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorRed,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ClinkPink,
    onPrimary = SurfaceLight,
    primaryContainer = ClinkPinkDark,
    onPrimaryContainer = ClinkPinkLight,
    secondary = ClinkTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = ClinkTealDark,
    onSecondaryContainer = ClinkTealLight,
    tertiary = ClinkNavyLight,
    onTertiary = SurfaceLight,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorRed,
    onError = SurfaceLight
)

@Composable
fun ClinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
