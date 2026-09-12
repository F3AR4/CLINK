package com.clink.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clink.app.domain.model.Money
import com.clink.app.presentation.theme.ClinkMotion
import com.clink.app.presentation.theme.CoinGold
import com.clink.app.presentation.theme.SuccessGreen
import com.clink.app.presentation.theme.SuccessGreenContainerDark
import com.clink.app.presentation.theme.SuccessGreenContainerLight

/**
 * Reusable celebratory feedback badge for the signature CLINK! moment.
 *
 * Displays "CLINK!" with the saved amount and celebratory piggy emoji,
 * entering with a spring bounce and fading out cleanly.
 */
@Composable
fun ClinkCelebrationBadge(
    visible: Boolean,
    amount: Money,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val isDark = MaterialTheme.colorScheme.background.run {
        // Simple luminance check or theme detection
        red * 0.299f + green * 0.587f + blue * 0.114f < 0.5f
    }

    val containerColor = if (isDark) SuccessGreenContainerDark else SuccessGreenContainerLight
    val borderColor = SuccessGreen.copy(alpha = 0.4f)
    val description = contentDescription ?: "CLINK! ${amount.formatDisplay()} saved to your piggy bank"

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = ClinkMotion.bouncySpring(),
            initialScale = 0.7f
        ) + fadeIn(animationSpec = tween(ClinkMotion.DurationFast)),
        exit = scaleOut(
            animationSpec = tween(ClinkMotion.DurationNormal),
            targetScale = 0.9f
        ) + fadeOut(animationSpec = tween(ClinkMotion.DurationNormal)),
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = containerColor,
            modifier = Modifier
                .shadow(8.dp, shape = MaterialTheme.shapes.extraLarge)
                .border(1.5.dp, borderColor, MaterialTheme.shapes.extraLarge)
                .semantics { this.contentDescription = description }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Joyful headline
                Text(
                    text = "CLINK!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Saved amount badge
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(SuccessGreen.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+ ${amount.formatDisplay()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "🐷",
                    fontSize = 18.sp
                )
            }
        }
    }
}
