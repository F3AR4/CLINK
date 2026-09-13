package com.clink.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.clink.app.domain.model.Money
import com.clink.app.presentation.theme.ClinkMotion
import kotlin.math.roundToLong

/**
 * Reusable animated monetary balance counter.
 *
 * Smoothly interpolates the displayed balance from the previous amount to the new amount
 * whenever [money] changes.
 *
 * CRITICAL ARCHITECTURAL GUARANTEE:
 * - Floating-point numbers are strictly confined to Compose visual animation interpolation.
 * - The final displayed amount is strictly bound to the authoritative [money] instance.
 * - Zero financial calculations are performed with Float or Double.
 */
@Composable
fun AnimatedMoneyDisplay(
    money: Money,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 32.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    alwaysShowDecimals: Boolean = false,
    durationMillis: Int = ClinkMotion.DurationSlow
) {
    val animatedPaise = remember { Animatable(money.paise.toFloat()) }

    LaunchedEffect(money.paise) {
        if (animatedPaise.value != money.paise.toFloat()) {
            animatedPaise.animateTo(
                targetValue = money.paise.toFloat(),
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = ClinkMotion.StandardEasing
                )
            )
        }
    }

    // When the animation completes or is idle, strictly use the authoritative money.paise.
    // During interpolation, round the continuous frame value to Long paise.
    val displayPaise = if (animatedPaise.isRunning) {
        animatedPaise.value.toDouble().roundToLong().coerceAtLeast(0L)
    } else {
        money.paise
    }

    val displayMoney = Money(displayPaise)

    MoneyDisplay(
        money = displayMoney,
        modifier = modifier.semantics {
            contentDescription = "Balance: ${money.formatDisplay()}"
            liveRegion = LiveRegionMode.Polite
        },
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        alwaysShowDecimals = alwaysShowDecimals
    )
}
