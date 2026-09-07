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
import com.clink.app.domain.model.Money

@Composable
fun MoneyDisplay(
    money: Money,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 32.sp
) {
    val rupees = money.paise / 100
    val remainingPaise = money.paise % 100

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "₹",
            fontSize = (fontSize.value * 0.8f).sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = String.format("%,d", rupees),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (remainingPaise > 0L) {
            Text(
                text = ".${remainingPaise.toString().padStart(2, '0')}",
                fontSize = (fontSize.value * 0.6f).sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
