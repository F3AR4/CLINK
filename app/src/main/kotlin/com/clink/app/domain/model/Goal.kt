package com.clink.app.domain.model

/**
 * Domain model representing a savings goal associated with a Pig.
 * The goal's progress is derived reactively from the Pig's authoritative balance
 * using [GoalProgressCalculator] rather than an independent mutable balance.
 */
data class Goal(
    val id: Long = 0L,
    val pigId: Long,
    val title: String,
    val targetAmount: Money,
    val savedAmount: Money = Money.ZERO,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
