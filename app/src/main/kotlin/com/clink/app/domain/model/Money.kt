package com.clink.app.domain.model

/**
 * Architectural Rule: All monetary amounts in CLINK are represented as 64-bit integers ([Long])
 * in the smallest fractional unit (paise for INR).
 *
 * ₹1 = 100 paise
 * ₹10 = 1,000 paise
 * ₹20 = 2,000 paise
 * ₹50 = 5,000 paise
 * ₹100 = 10,000 paise
 *
 * NEVER use [Double] or [Float] for monetary calculations to avoid floating-point rounding errors.
 */
@JvmInline
value class Money(val paise: Long) : Comparable<Money> {

    init {
        require(paise >= 0) { "Monetary amount cannot be negative: $paise paise" }
    }

    operator fun plus(other: Money): Money {
        return Money(Math.addExact(this.paise, other.paise))
    }

    operator fun minus(other: Money): Money {
        require(this.paise >= other.paise) {
            "Insufficient funds: cannot subtract ${other.paise} paise from ${this.paise} paise"
        }
        return Money(this.paise - other.paise)
    }

    override fun compareTo(other: Money): Int = this.paise.compareTo(other.paise)

    val isPositive: Boolean get() = this.paise > 0L
    val isZero: Boolean get() = this.paise == 0L

    /**
     * Formats the amount in Indian Rupees (e.g. ₹10, ₹10.50, ₹1,000).
     */
    fun formatDisplay(): String {
        val rupees = paise / 100
        val remainingPaise = paise % 100
        return if (remainingPaise == 0L) {
            "₹$rupees"
        } else {
            "₹$rupees.${remainingPaise.toString().padStart(2, '0')}"
        }
    }

    companion object {
        val ZERO = Money(0L)
        val zero: Money = ZERO
        val RS_10 = Money(1_000L)
        val RS_20 = Money(2_000L)
        val RS_50 = Money(5_000L)
        val RS_100 = Money(10_000L)

        fun fromRupees(rupees: Long): Money {
            require(rupees >= 0) { "Rupees cannot be negative: $rupees" }
            return Money(rupees * 100L)
        }

        fun fromPaise(paise: Long): Money = Money(paise)
    }
}
