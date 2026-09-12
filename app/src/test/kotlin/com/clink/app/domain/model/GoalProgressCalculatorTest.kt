package com.clink.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalProgressCalculatorTest {

    private val testGoal = Goal(
        id = 1L,
        pigId = 1L,
        title = "New Headphones",
        targetAmount = Money(500000L), // ₹5,000
        createdAt = 1000L
    )

    @Test
    fun `calculate returns zero progress when balance is zero`() {
        val progress = GoalProgressCalculator.calculate(
            goal = testGoal,
            pigBalance = Money.ZERO
        )

        assertEquals(0, progress.progressPercent)
        assertEquals(0f, progress.progressFraction, 0.001f)
        assertEquals(testGoal.targetAmount, progress.remainingAmount)
        assertFalse(progress.isCompleted)
    }

    @Test
    fun `calculate computes partial progress accurately`() {
        val progress = GoalProgressCalculator.calculate(
            goal = testGoal,
            pigBalance = Money(125000L) // ₹1,250 of ₹5,000 = 25%
        )

        assertEquals(25, progress.progressPercent)
        assertEquals(0.25f, progress.progressFraction, 0.001f)
        assertEquals(Money(375000L), progress.remainingAmount)
        assertFalse(progress.isCompleted)
    }

    @Test
    fun `calculate caps percentage at 99 percent when target not reached`() {
        // ₹4,999 of ₹5,000 is 99.98%, must NOT display 100% prematurely
        val progress = GoalProgressCalculator.calculate(
            goal = testGoal,
            pigBalance = Money(499900L)
        )

        assertEquals(99, progress.progressPercent)
        assertFalse(progress.isCompleted)
        assertEquals(Money(100L), progress.remainingAmount)
    }

    @Test
    fun `calculate marks goal completed when current equals target`() {
        val progress = GoalProgressCalculator.calculate(
            goal = testGoal,
            pigBalance = Money(500000L) // ₹5,000 of ₹5,000
        )

        assertEquals(100, progress.progressPercent)
        assertEquals(1.0f, progress.progressFraction, 0.001f)
        assertEquals(Money.ZERO, progress.remainingAmount)
        assertTrue(progress.isCompleted)
    }

    @Test
    fun `calculate clamps progress at 100 percent when current exceeds target`() {
        val progress = GoalProgressCalculator.calculate(
            goal = testGoal,
            pigBalance = Money(750000L) // ₹7,500 of ₹5,000
        )

        assertEquals(100, progress.progressPercent)
        assertEquals(1.0f, progress.progressFraction, 0.001f)
        assertEquals(Money.ZERO, progress.remainingAmount)
        assertTrue(progress.isCompleted)
    }

    @Test
    fun `calculate handles zero target defensively without division by zero`() {
        val zeroTargetGoal = testGoal.copy(targetAmount = Money.ZERO)
        val progress = GoalProgressCalculator.calculate(
            goal = zeroTargetGoal,
            pigBalance = Money(10000L)
        )

        assertEquals(0, progress.progressPercent)
        assertEquals(0f, progress.progressFraction, 0.001f)
        assertEquals(Money.ZERO, progress.remainingAmount)
        assertFalse(progress.isCompleted)
    }
}
