package com.clink.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Hardening tests for Goal progress calculations and boundary integrity.
 */
class GoalReliabilityTest {

    private val baseGoal = Goal(
        id = 1L,
        pigId = 1L,
        title = "Vacation Fund",
        targetAmount = Money.fromRupees(1000) // ₹1,000 = 100,000 paise
    )

    @Test
    fun `zero balance results in zero progress`() {
        val progress = GoalProgressCalculator.calculate(baseGoal, Money.ZERO)
        assertThat(progress.progressPercent).isEqualTo(0)
        assertThat(progress.progressFraction).isEqualTo(0.0f)
        assertThat(progress.isCompleted).isFalse()
        assertThat(progress.remainingAmount).isEqualTo(Money.fromRupees(1000))
    }

    @Test
    fun `partial progress calculates exact percentage and fraction`() {
        val balance = Money.fromRupees(500) // 50%
        val progress = GoalProgressCalculator.calculate(baseGoal, balance)
        assertThat(progress.progressPercent).isEqualTo(50)
        assertThat(progress.progressFraction).isEqualTo(0.5f)
        assertThat(progress.isCompleted).isFalse()
        assertThat(progress.remainingAmount).isEqualTo(Money.fromRupees(500))
    }

    @Test
    fun `progress is capped at 99 percent when balance is 1 paise short of target`() {
        // Target is 100,000 paise. Balance is 99,999 paise (1 paise short of ₹1,000)
        val nearTargetBalance = Money(99_999L)
        val progress = GoalProgressCalculator.calculate(baseGoal, nearTargetBalance)

        // Invariant: Must NEVER display 100% until target is fully met
        assertThat(progress.progressPercent).isEqualTo(99)
        assertThat(progress.progressFraction).isEqualTo(0.99f)
        assertThat(progress.isCompleted).isFalse()
        assertThat(progress.remainingAmount).isEqualTo(Money(1L))
    }

    @Test
    fun `exact target amount marks goal as 100 percent completed`() {
        val exactBalance = Money.fromRupees(1000)
        val progress = GoalProgressCalculator.calculate(baseGoal, exactBalance)
        assertThat(progress.progressPercent).isEqualTo(100)
        assertThat(progress.progressFraction).isEqualTo(1.0f)
        assertThat(progress.isCompleted).isTrue()
        assertThat(progress.remainingAmount).isEqualTo(Money.ZERO)
    }

    @Test
    fun `balance exceeding target clamps progress to 100 percent and zero remaining`() {
        val excessBalance = Money.fromRupees(2500) // 250%
        val progress = GoalProgressCalculator.calculate(baseGoal, excessBalance)
        assertThat(progress.progressPercent).isEqualTo(100)
        assertThat(progress.progressFraction).isEqualTo(1.0f)
        assertThat(progress.isCompleted).isTrue()
        assertThat(progress.remainingAmount).isEqualTo(Money.ZERO)
    }

    @Test
    fun `zero or negative target is handled defensively without throwing`() {
        val zeroTargetGoal = baseGoal.copy(targetAmount = Money.ZERO)
        val progress = GoalProgressCalculator.calculate(zeroTargetGoal, Money.fromRupees(100))
        assertThat(progress.progressPercent).isEqualTo(0)
        assertThat(progress.progressFraction).isEqualTo(0.0f)
        assertThat(progress.isCompleted).isFalse()
    }

    @Test
    fun `extremely large target amount and balance compute safely without overflow`() {
        val half = Long.MAX_VALUE / 4
        val largeTarget = Goal(
            id = 2L,
            pigId = 1L,
            title = "Billionaire Pig",
            targetAmount = Money(half * 2)
        )
        val largeBalance = Money(half) // Exactly 50%
        val progress = GoalProgressCalculator.calculate(largeTarget, largeBalance)
        assertThat(progress.progressPercent).isEqualTo(50)
        assertThat(progress.isCompleted).isFalse()
    }
}
