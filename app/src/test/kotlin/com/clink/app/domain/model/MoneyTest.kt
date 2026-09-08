package com.clink.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test
    fun `money constants represent correct paise amounts`() {
        assertThat(Money.RS_10.paise).isEqualTo(1000L)
        assertThat(Money.RS_20.paise).isEqualTo(2000L)
        assertThat(Money.RS_50.paise).isEqualTo(5000L)
        assertThat(Money.RS_100.paise).isEqualTo(10000L)
    }

    @Test
    fun `fromRupees creates correct paise value`() {
        val money = Money.fromRupees(150)
        assertThat(money.paise).isEqualTo(15000L)
    }

    @Test
    fun `negative paise throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(-100L)
        }
    }

    @Test
    fun `negative rupees throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.fromRupees(-10L)
        }
    }

    @Test
    fun `addition correctly sums paise`() {
        val sum = Money.RS_10 + Money.RS_20
        assertThat(sum.paise).isEqualTo(3000L)
        assertThat(sum.formatDisplay()).isEqualTo("₹30")
    }

    @Test
    fun `subtraction correctly calculates difference`() {
        val diff = Money.RS_100 - Money.RS_20
        assertThat(diff.paise).isEqualTo(8000L)
        assertThat(diff.formatDisplay()).isEqualTo("₹80")
    }

    @Test
    fun `subtraction with insufficient funds throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.RS_10 - Money.RS_20
        }
    }

    @Test
    fun `formatDisplay shows proper rupee and paise string`() {
        assertThat(Money.ZERO.formatDisplay()).isEqualTo("₹0")
        assertThat(Money.RS_10.formatDisplay()).isEqualTo("₹10")
        assertThat(Money.fromPaise(1050L).formatDisplay()).isEqualTo("₹10.50")
        assertThat(Money.fromPaise(1005L).formatDisplay()).isEqualTo("₹10.05")
    }

    @Test
    fun `comparison operators work as expected`() {
        assertThat(Money.RS_50).isGreaterThan(Money.RS_20)
        assertThat(Money.RS_10).isLessThan(Money.RS_100)
        assertThat(Money.fromRupees(10)).isEqualTo(Money.RS_10)
    }

    @Test
    fun `zero paise is accepted and equals ZERO`() {
        val zero = Money(0L)
        assertThat(zero).isEqualTo(Money.ZERO)
        assertThat(zero.paise).isEqualTo(0L)
    }

    @Test
    fun `addition overflowing Long MAX_VALUE throws ArithmeticException`() {
        val largeMoney = Money(Long.MAX_VALUE)
        assertThrows(ArithmeticException::class.java) {
            largeMoney + Money(1L)
        }
    }
}
