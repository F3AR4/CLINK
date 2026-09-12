package com.clink.app.domain.model

/**
 * Domain representation of a digital Piggy Bank ("Pig").
 * CLINK allows users to save in dedicated virtual pigs.
 */
data class Pig(
    val id: Long = 0L,
    val name: String,
    val balance: Money = Money.ZERO,
    val targetAmount: Money? = null,
    val iconName: String = "default_pig",
    val colorHex: String = "#E85D75",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val state: PigState
        get() = PigStateCalculator.calculate(balance)

    val progression: PigProgression
        get() = PigStateCalculator.calculateProgression(balance)
}
