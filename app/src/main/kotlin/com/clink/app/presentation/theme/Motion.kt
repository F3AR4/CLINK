package com.clink.app.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Reusable motion tokens and animation foundations for CLINK.
 */
object ClinkMotion {
    // Durations
    const val DurationFast = 150
    const val DurationNormal = 300
    const val DurationSlow = 500

    // Easings
    val StandardEasing: Easing = FastOutSlowInEasing
    val DecelerateEasing: Easing = LinearOutSlowInEasing
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Springs
    fun <T> bouncySpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}
