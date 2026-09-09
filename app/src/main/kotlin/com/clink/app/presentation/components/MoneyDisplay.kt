package com.clink.app.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.clink.app.domain.model.Money

/**
 * Reusable monetary amount display enforcing Indian currency formatting.
 * Displays authoritative integer-paise based amounts safely.
 */
@Composable
fun MoneyDisplay(
    money: Money,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 32.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    alwaysShowDecimals: Boolean = false
) {
    val rupees = money.paise / 100
    val remainingPaise = money.paise % 100

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        // Currency Symbol slightly scaled down for aesthetic balance
        Text(
            text = "₹",
            fontSize = (fontSize.value * 0.75f).sp,
            fontWeight = fontWeight,
            color = color
        )
        // Whole Rupees
        Text(
            text = String.format(Locale.getDefault(), "%,d", rupees),
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
        // Fractional Paise
        if (remainingPaise > 0L || alwaysShowDecimals) {
            Text(
                text = ".${remainingPaise.toString().padStart(2, '0')}",
                fontSize = (fontSize.value * 0.58f).sp,
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.85f)
            )
        }
    }
}
