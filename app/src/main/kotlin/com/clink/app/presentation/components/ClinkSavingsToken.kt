package com.clink.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clink.app.domain.model.Money
import com.clink.app.presentation.theme.ClinkNavy
import com.clink.app.presentation.theme.CoinGold
import com.clink.app.presentation.theme.CoinGoldDark
import com.clink.app.presentation.theme.CoinGoldLight
import com.clink.app.presentation.theme.CoinGoldRim

/**
 * Reusable Compose-native golden savings coin/token.
 *
 * Visually communicates the micro-savings amount (e.g. "₹10", "₹50", "₹100")
 * with a tactile metallic medallion aesthetic. Zero external bitmaps required.
 */
@Composable
fun ClinkSavingsToken(
    amount: Money,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showPlusPrefix: Boolean = false,
    contentDescription: String? = null
) {
    val description = contentDescription ?: "${if (showPlusPrefix) "+ " else ""}${amount.formatDisplay()} savings coin"

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val radius = w / 2f

            // 1. Outer rim border with depth
            drawCircle(
                color = CoinGoldRim,
                radius = radius,
                center = center
            )

            // 2. Metallic radial gradient body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CoinGoldLight, CoinGold, CoinGoldDark),
                    center = Offset(w * 0.35f, h * 0.35f),
                    radius = radius * 0.95f
                ),
                radius = radius * 0.93f,
                center = center
            )

            // 3. Inner decorative groove ring
            drawCircle(
                color = CoinGoldRim.copy(alpha = 0.6f),
                radius = radius * 0.78f,
                center = center,
                style = Stroke(width = w * 0.025f)
            )

            // 4. Subtle top-left gleam highlight arc
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = radius * 0.16f,
                center = Offset(w * 0.30f, h * 0.28f)
            )
        }

        // Amount label in center
        val displayText = if (showPlusPrefix) "+${amount.formatDisplay()}" else amount.formatDisplay()
        val textFontSize = when {
            displayText.length > 7 -> (size.value * 0.20f).sp
            displayText.length > 5 -> (size.value * 0.24f).sp
            else -> (size.value * 0.28f).sp
        }

        Text(
            text = displayText,
            fontSize = textFontSize,
            fontWeight = FontWeight.Black,
            color = ClinkNavy,
            maxLines = 1
        )
    }
}
