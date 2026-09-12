package com.clink.app.domain.model

/**
 * Deterministic domain calculator for computing [GoalProgress] from a [Goal]
 * and the Pig's authoritative savings [Money] balance.
 *
 * Rules:
 * - Operates strictly on integer paise ([Long]).
 * - Clamps progress percentage between 0 and 100.
 * - Prevents showing 100% before the monetary target is fully reached (capped at 99% if current < target).
 * - Handles zero or negative targets defensively.
 */
object GoalProgressCalculator {

    fun calculate(goal: Goal, pigBalance: Money): GoalProgress {
        val targetPaise = goal.targetAmount.paise
        val currentPaise = pigBalance.paise

        // Defensive handling for zero or non-positive targets
        if (targetPaise <= 0L) {
            return GoalProgress(
                goal = goal,
                currentAmount = pigBalance,
                targetAmount = goal.targetAmount,
                remainingAmount = Money.ZERO,
                progressPercent = 0,
                progressFraction = 0f,
                isCompleted = false
            )
        }

        val isCompleted = currentPaise >= targetPaise
        val remainingPaise = if (isCompleted) 0L else (targetPaise - currentPaise)
        val remainingAmount = Money(remainingPaise)

        val progressPercent = when {
            isCompleted -> 100
            currentPaise <= 0L -> 0
            else -> {
                // Integer math: (current * 100) / target
                val raw = ((currentPaise * 100L) / targetPaise).toInt()
                // Never show 100% if current < target
                raw.coerceIn(0, 99)
            }
        }

        val progressFraction = when {
            isCompleted -> 1.0f
            currentPaise <= 0L -> 0.0f
            else -> (progressPercent.toFloat() / 100f).coerceIn(0f, 0.99f)
        }

        return GoalProgress(
            goal = goal,
            currentAmount = pigBalance,
            targetAmount = goal.targetAmount,
            remainingAmount = remainingAmount,
            progressPercent = progressPercent,
            progressFraction = progressFraction,
            isCompleted = isCompleted
        )
    }
}
