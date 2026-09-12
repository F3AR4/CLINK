package com.clink.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PigStateCalculatorTest {

    @Test
    fun `zero balance calculates to NEW state`() {
        val state = PigStateCalculator.calculate(Money.ZERO)
        assertEquals(PigState.NEW, state)
    }

    @Test
    fun `small balance calculates to GROWING state`() {
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(Money(1L)))
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(Money.RS_10))
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(Money.RS_20))
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(Money.RS_50))
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(Money.RS_100))
    }

    @Test
    fun `healthy threshold boundary conditions`() {
        // Just below threshold: 49,999 paise (₹499.99) -> GROWING
        val justBelow = Money(49_999L)
        assertEquals(PigState.GROWING, PigStateCalculator.calculate(justBelow))

        // Exactly at threshold: 50,000 paise (₹500.00) -> HEALTHY
        val exact = Money(50_000L)
        assertEquals(PigState.HEALTHY, PigStateCalculator.calculate(exact))

        // Just above threshold: 50,001 paise (₹500.01) -> HEALTHY
        val justAbove = Money(50_001L)
        assertEquals(PigState.HEALTHY, PigStateCalculator.calculate(justAbove))
    }

    @Test
    fun `full threshold boundary conditions`() {
        // Just below threshold: 199,999 paise (₹1,999.99) -> HEALTHY
        val justBelow = Money(199_999L)
        assertEquals(PigState.HEALTHY, PigStateCalculator.calculate(justBelow))

        // Exactly at threshold: 200,000 paise (₹2,000.00) -> FULL
        val exact = Money(200_000L)
        assertEquals(PigState.FULL, PigStateCalculator.calculate(exact))

        // Just above threshold: 200,001 paise (₹2,000.01) -> FULL
        val justAbove = Money(200_001L)
        assertEquals(PigState.FULL, PigStateCalculator.calculate(justAbove))

        // Well above threshold
        val large = Money.fromRupees(10_000L)
        assertEquals(PigState.FULL, PigStateCalculator.calculate(large))
    }

    @Test
    fun `calculation is deterministic for identical balances`() {
        val testBalances = listOf(
            Money.ZERO,
            Money.fromRupees(10),
            Money.fromRupees(499),
            Money.fromRupees(500),
            Money.fromRupees(1999),
            Money.fromRupees(2000),
            Money.fromRupees(5000)
        )

        for (balance in testBalances) {
            val expected = PigStateCalculator.calculate(balance)
            repeat(50) {
                assertEquals(expected, PigStateCalculator.calculate(balance))
            }
        }
    }

    @Test
    fun `progression fractions are calculated accurately across tiers`() {
        // NEW at ₹0
        val newProgression = PigStateCalculator.calculateProgression(Money.ZERO)
        assertEquals(PigState.NEW, newProgression.state)
        assertEquals(0.0f, newProgression.progressToNextTier, 0.001f)
        assertEquals(PigStateCalculator.THRESHOLD_HEALTHY, newProgression.nextThreshold)

        // GROWING halfway: ₹250 of ₹500
        val growingMid = PigStateCalculator.calculateProgression(Money.fromRupees(250))
        assertEquals(PigState.GROWING, growingMid.state)
        assertEquals(0.5f, growingMid.progressToNextTier, 0.001f)
        assertEquals(PigStateCalculator.THRESHOLD_HEALTHY, growingMid.nextThreshold)

        // HEALTHY start: ₹500
        val healthyStart = PigStateCalculator.calculateProgression(Money.fromRupees(500))
        assertEquals(PigState.HEALTHY, healthyStart.state)
        assertEquals(0.0f, healthyStart.progressToNextTier, 0.001f)
        assertEquals(PigStateCalculator.THRESHOLD_FULL, healthyStart.nextThreshold)

        // HEALTHY halfway: ₹1250 (500 + 750 / 1500)
        val healthyMid = PigStateCalculator.calculateProgression(Money.fromRupees(1250))
        assertEquals(PigState.HEALTHY, healthyMid.state)
        assertEquals(0.5f, healthyMid.progressToNextTier, 0.001f)
        assertEquals(PigStateCalculator.THRESHOLD_FULL, healthyMid.nextThreshold)

        // FULL: ₹2000+
        val fullProgression = PigStateCalculator.calculateProgression(Money.fromRupees(2000))
        assertEquals(PigState.FULL, fullProgression.state)
        assertEquals(1.0f, fullProgression.progressToNextTier, 0.001f)
        assertNull(fullProgression.nextThreshold)
    }

    @Test
    fun `Pig domain model exposes derived state and progression`() {
        val pig = Pig(
            id = 1L,
            name = "Test Pig",
            balance = Money.fromRupees(600)
        )

        assertEquals(PigState.HEALTHY, pig.state)
        assertEquals(PigState.HEALTHY, pig.progression.state)
        assertEquals(Money.fromRupees(600), pig.progression.currentBalance)
        assertEquals(PigStateCalculator.THRESHOLD_FULL, pig.progression.nextThreshold)
    }
}
