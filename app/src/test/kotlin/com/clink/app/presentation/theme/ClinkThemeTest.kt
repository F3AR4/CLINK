package com.clink.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClinkThemeTest {

    @Test
    fun brandColors_areDefinedCorrectly() {
        assertThat(ClinkPink).isEqualTo(Color(0xFFE85D75))
        assertThat(ClinkNavy).isEqualTo(Color(0xFF172033))
        assertThat(ClinkTeal).isEqualTo(Color(0xFF35B8A6))
        assertThat(CoinGold).isEqualTo(Color(0xFFFFB703))
    }

    @Test
    fun dimensions_haveConsistentSpacingTokens() {
        val dimens = Dimensions()
        assertThat(dimens.spacingNone).isEqualTo(0.dp)
        assertThat(dimens.spacingXs).isLessThan(dimens.spacingSm)
        assertThat(dimens.spacingSm).isLessThan(dimens.spacingMd)
        assertThat(dimens.spacingMd).isLessThan(dimens.spacingLg)
        assertThat(dimens.spacingLg).isLessThan(dimens.spacingXl)
        assertThat(dimens.spacingXl).isLessThan(dimens.spacingXxl)
        assertThat(dimens.spacingXxl).isLessThan(dimens.spacingXxxl)
        assertThat(dimens.minTouchTarget).isEqualTo(48.dp)
        assertThat(dimens.buttonHeight).isAtLeast(dimens.minTouchTarget)
        assertThat(dimens.chipHeight).isAtLeast(dimens.minTouchTarget)
        assertThat(dimens.amountChipHeight).isAtLeast(dimens.minTouchTarget)
    }

    @Test
    fun shapes_haveConsistentCornerRadii() {
        assertThat(Shapes.extraSmall.topStart).isNotNull()
        assertThat(Shapes.small.topStart).isNotNull()
        assertThat(Shapes.medium.topStart).isNotNull()
        assertThat(Shapes.large.topStart).isNotNull()
        assertThat(Shapes.extraLarge.topStart).isNotNull()
    }

    @Test
    fun motion_durationsArePositiveAndOrdered() {
        assertThat(ClinkMotion.DurationFast).isLessThan(ClinkMotion.DurationNormal)
        assertThat(ClinkMotion.DurationNormal).isLessThan(ClinkMotion.DurationSlow)
        assertThat(ClinkMotion.StandardEasing).isNotNull()
    }
}
