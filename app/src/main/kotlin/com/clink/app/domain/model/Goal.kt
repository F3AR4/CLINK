package com.clink.app.domain.model

/**
 * Domain model representing a savings goal associated with a Pig.
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
) {
    val progressPercentage: Float
        get() = if (targetAmount.paise == 0L) 1.0f
        else (savedAmount.paise.toFloat() / targetAmount.paise.toFloat()).coerceIn(0f, 1f)
}
