package com.clink.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clink.app.domain.model.PigState
import com.clink.app.presentation.theme.ClinkNavy
import com.clink.app.presentation.theme.ClinkPink
import com.clink.app.presentation.theme.ClinkPinkDark
import com.clink.app.presentation.theme.ClinkPinkLight
import com.clink.app.presentation.theme.CoinGold
import com.clink.app.presentation.theme.CoinGoldLight
import com.clink.app.presentation.theme.PiggyBlush
import com.clink.app.presentation.theme.PiggyEarInside

/**
 * Delightful, Compose-native vector illustration of the CLINK piggy bank mascot.
 * Dynamically reflects the [PigState] progression (NEW, GROWING, HEALTHY, FULL)
 * with expressive facial features, coin visibility, and celebratory accents.
 */
@Composable
fun ClinkPigIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    state: PigState = PigState.GROWING,
    contentDescription: String? = null
) {
    val semanticModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Canvas(modifier = semanticModifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Golden Coin (peeking from slot depending on state)
        if (state != PigState.NEW) {
            val coinWidth = when (state) {
                PigState.FULL -> w * 0.32f
                PigState.HEALTHY -> w * 0.30f
                else -> w * 0.28f
            }
            val coinHeight = when (state) {
                PigState.FULL -> h * 0.30f
                PigState.HEALTHY -> h * 0.28f
                else -> h * 0.26f
            }
            val coinTop = when (state) {
                PigState.FULL -> h * 0.02f
                PigState.HEALTHY -> h * 0.03f
                else -> h * 0.05f
            }
            val coinLeft = (w - coinWidth) / 2f

            drawRoundRect(
                color = CoinGold,
                topLeft = Offset(coinLeft, coinTop),
                size = Size(coinWidth, coinHeight),
                cornerRadius = CornerRadius(coinWidth / 2, coinWidth / 2)
            )
            // Coin gleam
            drawRoundRect(
                color = CoinGoldLight,
                topLeft = Offset(coinLeft + coinWidth * 0.2f, coinTop + coinHeight * 0.15f),
                size = Size(coinWidth * 0.6f, coinHeight * 0.4f),
                cornerRadius = CornerRadius(coinWidth * 0.3f, coinWidth * 0.3f)
            )

            // Extra shine for HEALTHY / FULL
            if (state == PigState.HEALTHY || state == PigState.FULL) {
                drawCircle(
                    color = Color.White,
                    radius = coinWidth * 0.08f,
                    center = Offset(coinLeft + coinWidth * 0.25f, coinTop + coinHeight * 0.22f)
                )
            }
        }

        // 2. Ears (Left and Right)
        val leftEarPath = Path().apply {
            moveTo(w * 0.22f, h * 0.35f)
            quadraticTo(w * 0.12f, h * 0.14f, w * 0.34f, h * 0.22f)
            close()
        }
        drawPath(leftEarPath, color = ClinkPinkDark)

        val leftEarInnerPath = Path().apply {
            moveTo(w * 0.23f, h * 0.32f)
            quadraticTo(w * 0.16f, h * 0.18f, w * 0.32f, h * 0.24f)
            close()
        }
        drawPath(leftEarInnerPath, color = PiggyEarInside)

        val rightEarPath = Path().apply {
            moveTo(w * 0.78f, h * 0.35f)
            quadraticTo(w * 0.88f, h * 0.14f, w * 0.66f, h * 0.22f)
            close()
        }
        drawPath(rightEarPath, color = ClinkPinkDark)

        val rightEarInnerPath = Path().apply {
            moveTo(w * 0.77f, h * 0.32f)
            quadraticTo(w * 0.84f, h * 0.18f, w * 0.68f, h * 0.24f)
            close()
        }
        drawPath(rightEarInnerPath, color = PiggyEarInside)

        // 3. Piggy Body (Chubby round circle)
        val bodyRadius = when (state) {
            PigState.FULL -> w * 0.44f
            PigState.HEALTHY -> w * 0.43f
            else -> w * 0.42f
        }
        drawCircle(
            color = ClinkPink,
            radius = bodyRadius,
            center = Offset(w * 0.5f, h * 0.58f)
        )

        // Subtle soft highlight on top-left of body
        drawCircle(
            color = ClinkPinkLight.copy(alpha = 0.4f),
            radius = bodyRadius * 0.85f,
            center = Offset(w * 0.46f, h * 0.54f)
        )

        // 4. Coin Slot (Dark horizontal slit on forehead)
        drawRoundRect(
            color = ClinkPinkDark,
            topLeft = Offset(w * 0.42f, h * 0.22f),
            size = Size(w * 0.16f, h * 0.045f),
            cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
        )

        // 5. Facial Expressions tailored to PigState
        when (state) {
            PigState.NEW -> {
                // Calm, innocent gentle round eyes
                val eyeRadius = w * 0.032f
                drawCircle(color = ClinkNavy, radius = eyeRadius, center = Offset(w * 0.36f, h * 0.52f))
                drawCircle(color = ClinkNavy, radius = eyeRadius, center = Offset(w * 0.64f, h * 0.52f))
            }
            PigState.GROWING -> {
                // Wide sparkling eyes with bright catchlights
                val eyeRadius = w * 0.038f
                drawCircle(color = ClinkNavy, radius = eyeRadius, center = Offset(w * 0.36f, h * 0.52f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.35f, h * 0.51f))

                drawCircle(color = ClinkNavy, radius = eyeRadius, center = Offset(w * 0.64f, h * 0.52f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.63f, h * 0.51f))
            }
            PigState.HEALTHY -> {
                // Joyous happy curved eye arcs (^ ^)
                val strokeWidth = w * 0.028f
                val eyeArcSize = Size(w * 0.09f, h * 0.06f)
                drawArc(
                    color = ClinkNavy,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.31f, h * 0.49f),
                    size = eyeArcSize,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = ClinkNavy,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.59f, h * 0.49f),
                    size = eyeArcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
            PigState.FULL -> {
                // Celebratory wink & happy arc (> ^)
                val strokeWidth = w * 0.028f
                // Left eye: cheerful wink arrow (>)
                val leftWink = Path().apply {
                    moveTo(w * 0.31f, h * 0.50f)
                    lineTo(w * 0.37f, h * 0.525f)
                    lineTo(w * 0.31f, h * 0.55f)
                }
                drawPath(leftWink, color = ClinkNavy, style = Stroke(width = strokeWidth))

                // Right eye: happy upward curved arc (^)
                drawArc(
                    color = ClinkNavy,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.59f, h * 0.49f),
                    size = Size(w * 0.09f, h * 0.06f),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // 6. Rosy Blush Cheeks
        val blushAlpha = when (state) {
            PigState.NEW -> 0.45f
            PigState.GROWING -> 0.80f
            PigState.HEALTHY -> 0.90f
            PigState.FULL -> 1.0f
        }
        val blushRadius = w * 0.065f
        drawCircle(
            color = PiggyBlush.copy(alpha = blushAlpha),
            radius = blushRadius,
            center = Offset(w * 0.26f, h * 0.60f)
        )
        drawCircle(
            color = PiggyBlush.copy(alpha = blushAlpha),
            radius = blushRadius,
            center = Offset(w * 0.74f, h * 0.60f)
        )

        // 7. Cute Snout (Horizontal pill in center)
        val snoutWidth = w * 0.28f
        val snoutHeight = h * 0.18f
        val snoutTop = h * 0.60f
        val snoutLeft = (w - snoutWidth) / 2f
        drawRoundRect(
            color = ClinkPinkDark,
            topLeft = Offset(snoutLeft, snoutTop),
            size = Size(snoutWidth, snoutHeight),
            cornerRadius = CornerRadius(snoutHeight / 2, snoutHeight / 2)
        )

        // Nostrils (Two small vertical rounded slots)
        val nostrilWidth = snoutWidth * 0.18f
        val nostrilHeight = snoutHeight * 0.45f
        val nostrilTop = snoutTop + (snoutHeight - nostrilHeight) / 2f
        drawRoundRect(
            color = ClinkPink,
            topLeft = Offset(w * 0.44f - nostrilWidth / 2f, nostrilTop),
            size = Size(nostrilWidth, nostrilHeight),
            cornerRadius = CornerRadius(nostrilWidth / 2, nostrilWidth / 2)
        )
        drawRoundRect(
            color = ClinkPink,
            topLeft = Offset(w * 0.56f - nostrilWidth / 2f, nostrilTop),
            size = Size(nostrilWidth, nostrilHeight),
            cornerRadius = CornerRadius(nostrilWidth / 2, nostrilWidth / 2)
        )

        // 8. State-specific Accents (Star for Healthy, Crown for Full)
        if (state == PigState.HEALTHY) {
            // Cute 4-point sparkle star near right ear
            val starPath = Path().apply {
                val cx = w * 0.88f
                val cy = h * 0.20f
                val r = w * 0.07f
                val innerR = r * 0.35f
                moveTo(cx, cy - r)
                quadraticTo(cx, cy, cx + r, cy)
                quadraticTo(cx, cy, cx, cy + r)
                quadraticTo(cx, cy, cx - r, cy)
                quadraticTo(cx, cy, cx, cy - r)
                close()
            }
            drawPath(starPath, color = CoinGold)
        } else if (state == PigState.FULL) {
            // Mini Golden Crown resting atop the pig's head
            val crownLeft = w * 0.36f
            val crownWidth = w * 0.28f
            val crownBottom = h * 0.20f
            val crownHeight = h * 0.14f

            val crownPath = Path().apply {
                moveTo(crownLeft, crownBottom)
                lineTo(crownLeft, crownBottom - crownHeight * 0.7f)
                lineTo(crownLeft + crownWidth * 0.25f, crownBottom - crownHeight * 0.35f)
                lineTo(crownLeft + crownWidth * 0.5f, crownBottom - crownHeight)
                lineTo(crownLeft + crownWidth * 0.75f, crownBottom - crownHeight * 0.35f)
                lineTo(crownLeft + crownWidth, crownBottom - crownHeight * 0.7f)
                lineTo(crownLeft + crownWidth, crownBottom)
                close()
            }
            drawPath(crownPath, color = CoinGold)

            // Crown jewel highlights
            drawCircle(color = CoinGoldLight, radius = w * 0.02f, center = Offset(crownLeft + crownWidth * 0.5f, crownBottom - crownHeight))
            drawCircle(color = CoinGoldLight, radius = w * 0.015f, center = Offset(crownLeft, crownBottom - crownHeight * 0.7f))
            drawCircle(color = CoinGoldLight, radius = w * 0.015f, center = Offset(crownLeft + crownWidth, crownBottom - crownHeight * 0.7f))
        }
    }
}
