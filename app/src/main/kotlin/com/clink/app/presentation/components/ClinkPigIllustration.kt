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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
 * Displays a cute pink piggy with a shiny gold coin in its slot.
 */
@Composable
fun ClinkPigIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Golden Coin peeking from slot
        val coinWidth = w * 0.28f
        val coinHeight = h * 0.28f
        val coinTop = h * 0.04f
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
        val bodyRadius = w * 0.42f
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

        // 5. Friendly Eyes (Dark sparkling ovals)
        val eyeRadius = w * 0.038f
        drawCircle(
            color = ClinkNavy,
            radius = eyeRadius,
            center = Offset(w * 0.36f, h * 0.52f)
        )
        // Eye catchlights
        drawCircle(
            color = Color.White,
            radius = eyeRadius * 0.4f,
            center = Offset(w * 0.35f, h * 0.51f)
        )

        drawCircle(
            color = ClinkNavy,
            radius = eyeRadius,
            center = Offset(w * 0.64f, h * 0.52f)
        )
        // Eye catchlights
        drawCircle(
            color = Color.White,
            radius = eyeRadius * 0.4f,
            center = Offset(w * 0.63f, h * 0.51f)
        )

        // 6. Rosy Blush Cheeks
        val blushRadius = w * 0.065f
        drawCircle(
            color = PiggyBlush.copy(alpha = 0.85f),
            radius = blushRadius,
            center = Offset(w * 0.26f, h * 0.60f)
        )
        drawCircle(
            color = PiggyBlush.copy(alpha = 0.85f),
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
    }
}
