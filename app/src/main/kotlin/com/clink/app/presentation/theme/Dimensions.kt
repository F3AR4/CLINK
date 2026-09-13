package com.clink.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard dimensions and spacing tokens for CLINK.
 * Avoids arbitrary padding values across UI screens.
 */
data class Dimensions(
    val spacingNone: Dp = 0.dp,
    val spacingXxs: Dp = 2.dp,
    val spacingXs: Dp = 4.dp,
    val spacingSm: Dp = 8.dp,
    val spacingMd: Dp = 12.dp,
    val spacingLg: Dp = 16.dp,
    val spacingXl: Dp = 24.dp,
    val spacingXxl: Dp = 32.dp,
    val spacingXxxl: Dp = 48.dp,

    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 56.dp,
    val chipHeight: Dp = 48.dp,
    val amountChipHeight: Dp = 58.dp,

    val iconXs: Dp = 16.dp,
    val iconSm: Dp = 20.dp,
    val iconMd: Dp = 24.dp,
    val iconLg: Dp = 32.dp,
    val iconXl: Dp = 48.dp,
    val iconHero: Dp = 72.dp,
    val mascotHero: Dp = 88.dp,
    val mascotSplash: Dp = 140.dp,

    val elevationLevel0: Dp = 0.dp,
    val elevationLevel1: Dp = 2.dp,
    val elevationLevel2: Dp = 4.dp,
    val elevationLevel3: Dp = 8.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }

/**
 * Convenient accessor for Clink dimensions:
 * `ClinkTheme.dimens.spacingMd`
 */
object ClinkDimens {
    val current: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
}
