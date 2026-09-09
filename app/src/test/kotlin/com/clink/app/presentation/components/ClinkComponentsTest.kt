package com.clink.app.presentation.components

import com.clink.app.domain.model.Money
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClinkComponentsTest {

    @Test
    fun standardDenominations_areAvailableForQuickSelection() {
        val denominations = listOf(Money.RS_10, Money.RS_20, Money.RS_50, Money.RS_100)

        assertThat(denominations).hasSize(4)
        assertThat(denominations[0].paise).isEqualTo(1000L)
        assertThat(denominations[1].paise).isEqualTo(2000L)
        assertThat(denominations[2].paise).isEqualTo(5000L)
        assertThat(denominations[3].paise).isEqualTo(10000L)
    }

    @Test
    fun moneyDisplayFormatting_calculatesRupeesAndPaiseCorrectly() {
        val amount = Money.fromRupees(150)
        val rupees = amount.paise / 100
        val paise = amount.paise % 100

        assertThat(rupees).isEqualTo(150L)
        assertThat(paise).isEqualTo(0L)
    }

    @Test
    fun moneyWithPaise_extractsRemainingFractionCorrectly() {
        val amount = Money(1575L) // ₹15.75
        val rupees = amount.paise / 100
        val paise = amount.paise % 100

        assertThat(rupees).isEqualTo(15L)
        assertThat(paise).isEqualTo(75L)
    }
}
