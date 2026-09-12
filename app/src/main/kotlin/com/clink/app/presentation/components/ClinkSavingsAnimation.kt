package com.clink.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.clink.app.domain.model.Money
import com.clink.app.presentation.theme.ClinkMotion
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Stages of the savings interaction animation.
 */
enum class SavingsAnimationStage {
    IDLE,
    FLIGHT,
    IMPACT,
    CELEBRATION,
    COMPLETED
}

/**
 * Orchestrator component that animates the golden savings token launching
 * from the action area into the piggy bank mascot, triggering the pig's reaction
 * and displaying the signature "CLINK!" celebratory feedback.
 *
 * @param isPlaying Whether the animation is currently active.
 * @param amount The authoritative [Money] amount being saved.
 * @param startOffset The screen/container coordinate where the flight begins (e.g. Save button).
 * @param targetOffset The screen/container coordinate of the piggy bank coin slot.
 * @param onPigReaction Callback fired when the coin impacts the piggy bank.
 * @param onAnimationComplete Callback fired when the full sequence finishes.
 */
@Composable
fun ClinkSavingsAnimationOverlay(
    isPlaying: Boolean,
    amount: Money,
    startOffset: Offset,
    targetOffset: Offset,
    modifier: Modifier = Modifier,
    onPigReaction: () -> Unit = {},
    onAnimationComplete: () -> Unit = {}
) {
    if (!isPlaying) return

    val density = LocalDensity.current
    val flightProgress = remember { Animatable(0f) }
    var currentStage by remember { mutableStateOf(SavingsAnimationStage.FLIGHT) }
    var showCelebrationBadge by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            currentStage = SavingsAnimationStage.FLIGHT
            flightProgress.snapTo(0f)

            // Stage 1: Flight from action area toward pig slot (~450ms)
            flightProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ClinkMotion.DurationFlight,
                    easing = ClinkMotion.FlightEasing
                )
            )

            // Stage 2: Coin enters slot -> Piggy reacts!
            currentStage = SavingsAnimationStage.IMPACT
            onPigReaction()

            // Stage 3: Signature "CLINK!" celebration badge appears
            currentStage = SavingsAnimationStage.CELEBRATION
            showCelebrationBadge = true

            // Linger celebration for tactile satisfaction
            delay(ClinkMotion.DurationCelebration.toLong())

            showCelebrationBadge = false
            delay(ClinkMotion.DurationNormal.toLong())

            // Stage 4: Finished -> Trigger navigation/state reset
            currentStage = SavingsAnimationStage.COMPLETED
            onAnimationComplete()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val t = flightProgress.value

        // Coin flight rendering during FLIGHT stage
        if (t < 1f && currentStage == SavingsAnimationStage.FLIGHT) {
            // Natural parabolic arc: subtle horizontal sinusoidal deviation
            val arcArcDeviation = sin(Math.PI * t).toFloat() * with(density) { 24.dp.toPx() }
            val currentX = (startOffset.x + (targetOffset.x - startOffset.x) * t) + arcArcDeviation
            val currentY = startOffset.y + (targetOffset.y - startOffset.y) * t

            // Scale down as entering the coin slot
            val coinScale = if (t > 0.65f) {
                1f - ((t - 0.65f) / 0.35f) * 0.55f
            } else {
                1f
            }

            // Fade out at the very end of entering
            val coinAlpha = if (t > 0.85f) {
                1f - ((t - 0.85f) / 0.15f)
            } else {
                1f
            }

            ClinkSavingsToken(
                amount = amount,
                size = 64.dp,
                modifier = Modifier
                    .offset {
                        // Offset center of token (64dp / 2 = 32dp)
                        val halfSizePx = with(density) { 32.dp.toPx() }
                        IntOffset(
                            x = (currentX - halfSizePx).roundToInt(),
                            y = (currentY - halfSizePx).roundToInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = coinScale
                        scaleY = coinScale
                        alpha = coinAlpha
                    }
            )
        }

        // Celebratory "CLINK!" badge centered near the impact zone
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    // Position slightly below target offset or at a comfortable top position
                    val topPaddingPx = if (targetOffset.y > 0f) {
                        (targetOffset.y + with(density) { 60.dp.toPx() }).roundToInt()
                    } else {
                        with(density) { 140.dp.toPx() }.roundToInt()
                    }
                    IntOffset(x = 0, y = topPaddingPx)
                }
        ) {
            ClinkCelebrationBadge(
                visible = showCelebrationBadge,
                amount = amount
            )
        }
    }
}
