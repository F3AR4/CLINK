package com.clink.app.domain.model

/**
 * Centralized deterministic engine for deriving a Pig's progression state
 * and milestone progress from its accumulated savings.
 *
 * Monetary thresholds are defined using [Money] to guarantee zero floating-point
 * rounding errors or discrepancies.
 *
 * Progression Tiers:
 * - [PigState.NEW]: Exactly ₹0 (0 paise)
 * - [PigState.GROWING]: ₹0.01 to ₹499.99 (1 to 49,999 paise)
 * - [PigState.HEALTHY]: ₹500.00 to ₹1,999.99 (50,000 to 199,999 paise)
 * - [PigState.FULL]: ₹2,000.00+ (200,000+ paise)
 */
object PigStateCalculator {

    val THRESHOLD_HEALTHY = Money.fromRupees(500) // 50,000 paise
    val THRESHOLD_FULL = Money.fromRupees(2000) // 200,000 paise

    /**
     * Deterministically calculates the [PigState] for a given balance.
     */
    fun calculate(balance: Money): PigState {
        return when {
            balance.paise == 0L -> PigState.NEW
            balance.paise < THRESHOLD_HEALTHY.paise -> PigState.GROWING
            balance.paise < THRESHOLD_FULL.paise -> PigState.HEALTHY
            else -> PigState.FULL
        }
    }

    /**
     * Calculates rich progression information including the next monetary milestone
     * and a clamped [0.0f..1.0f] progression fraction.
     */
    fun calculateProgression(balance: Money): PigProgression {
        val state = calculate(balance)
        val (nextThreshold, progress) = when (state) {
            PigState.NEW -> {
                Pair(THRESHOLD_HEALTHY, 0.0f)
            }
            PigState.GROWING -> {
                val fraction = balance.paise.toFloat() / THRESHOLD_HEALTHY.paise.toFloat()
                Pair(THRESHOLD_HEALTHY, fraction.coerceIn(0.0f, 1.0f))
            }
            PigState.HEALTHY -> {
                val currentInTier = (balance.paise - THRESHOLD_HEALTHY.paise).toFloat()
                val tierSpan = (THRESHOLD_FULL.paise - THRESHOLD_HEALTHY.paise).toFloat()
                val fraction = currentInTier / tierSpan
                Pair(THRESHOLD_FULL, fraction.coerceIn(0.0f, 1.0f))
            }
            PigState.FULL -> {
                Pair(null, 1.0f)
            }
        }

        return PigProgression(
            state = state,
            currentBalance = balance,
            nextThreshold = nextThreshold,
            progressToNextTier = progress
        )
    }
}
