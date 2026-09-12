package com.clink.app.domain.model

/**
 * Encapsulates the derived progress of a [Goal] against the Pig's authoritative balance.
 *
 * All monetary amounts are strictly integer paise ([Money]).
 * [progressPercent] is an integer from 0 to 100.
 * [progressFraction] is a clamped [0f, 1f] float strictly for Compose progress indicators.
 */
data class GoalProgress(
    val goal: Goal,
    val currentAmount: Money,
    val targetAmount: Money,
    val remainingAmount: Money,
    val progressPercent: Int,
    val progressFraction: Float,
    val isCompleted: Boolean
)
