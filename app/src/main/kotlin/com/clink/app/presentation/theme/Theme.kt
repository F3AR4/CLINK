package com.clink.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ClinkPink,
    onPrimary = SurfaceLight,
    primaryContainer = ClinkPinkLight.copy(alpha = 0.25f),
    onPrimaryContainer = ClinkPinkDark,

    secondary = ClinkTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = SuccessGreenContainerLight,
    onSecondaryContainer = ClinkTealDark,

    tertiary = ClinkNavy,
    onTertiary = SurfaceLight,
    tertiaryContainer = ClinkNavyLight.copy(alpha = 0.15f),
    onTertiaryContainer = ClinkNavyDark,

    background = BackgroundLight,
    onBackground = OnSurfaceLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,

    error = ErrorRed,
    onError = SurfaceLight,
    errorContainer = ErrorRedContainerLight,
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = ClinkPinkLight,
    onPrimary = ClinkNavyDark,
    primaryContainer = ClinkPinkDark,
    onPrimaryContainer = ClinkPinkLight,

    secondary = ClinkTealLight,
    onSecondary = ClinkNavyDark,
    secondaryContainer = SuccessGreenContainerDark,
    onSecondaryContainer = ClinkTealLight,

    tertiary = ClinkNavyLight,
    onTertiary = OnSurfaceDark,
    tertiaryContainer = ClinkNavySurface,
    onTertiaryContainer = ClinkPinkLight,

    background = BackgroundDark,
    onBackground = OnSurfaceDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,

    error = ErrorRed,
    onError = SurfaceLight,
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = ErrorRed
)

object ClinkTheme {
    val dimens: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current

    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}

@Composable
fun ClinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dimensions: Dimensions = Dimensions(),
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

    CompositionLocalProvider(
        LocalDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
