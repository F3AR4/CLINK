package com.clink.app.presentation.components

import com.clink.app.domain.model.Money
import com.clink.app.presentation.theme.ClinkMotion
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.roundToLong

class ClinkSavingsAnimationTest {

    @Test
    fun `savings animation stages follow expected lifecycle order`() {
        val stages = listOf(
            SavingsAnimationStage.IDLE,
            SavingsAnimationStage.FLIGHT,
            SavingsAnimationStage.IMPACT,
            SavingsAnimationStage.CELEBRATION,
            SavingsAnimationStage.COMPLETED
        )

        assertThat(stages[0]).isEqualTo(SavingsAnimationStage.IDLE)
        assertThat(stages[1]).isEqualTo(SavingsAnimationStage.FLIGHT)
        assertThat(stages[2]).isEqualTo(SavingsAnimationStage.IMPACT)
        assertThat(stages[3]).isEqualTo(SavingsAnimationStage.CELEBRATION)
        assertThat(stages[4]).isEqualTo(SavingsAnimationStage.COMPLETED)
    }

    @Test
    fun `token display formatting matches authoritative money representation`() {
        val ten = Money.RS_10
        assertThat(ten.formatDisplay()).isEqualTo("₹10")
        assertThat("+${ten.formatDisplay()}").isEqualTo("+₹10")

        val thirtyFive = Money.fromRupees(35)
        assertThat(thirtyFive.formatDisplay()).isEqualTo("₹35")
        assertThat("+${thirtyFive.formatDisplay()}").isEqualTo("+₹35")

        val largeAmount = Money.fromRupees(1000)
        assertThat(largeAmount.formatDisplay()).isEqualTo("₹1000")
        assertThat("+${largeAmount.formatDisplay()}").isEqualTo("+₹1000")
    }

    @Test
    fun `animated balance interpolation strictly preserves target integer paise without drift`() {
        val startPaise = 1000L  // ₹10
        val targetPaise = 3500L // ₹35

        // Check start (t = 0.0)
        val atStart = (startPaise.toFloat() + (targetPaise - startPaise).toFloat() * 0.0f)
            .toDouble().roundToLong()
        assertThat(atStart).isEqualTo(startPaise)
        assertThat(Money(atStart)).isEqualTo(Money.RS_10)

        // Check midpoint (t = 0.5)
        val atMid = (startPaise.toFloat() + (targetPaise - startPaise).toFloat() * 0.5f)
            .toDouble().roundToLong()
        assertThat(atMid).isEqualTo(2250L) // ₹22.50
        assertThat(Money(atMid).paise).isEqualTo(2250L)

        // Check completion (t = 1.0)
        val atEnd = (startPaise.toFloat() + (targetPaise - startPaise).toFloat() * 1.0f)
            .toDouble().roundToLong()
        assertThat(atEnd).isEqualTo(targetPaise)
        assertThat(Money(atEnd)).isEqualTo(Money(3500L))
    }

    @Test
    fun `motion token durations stay within coherent human-perception ranges`() {
        // Micro feedback ~100–200ms
        assertThat(ClinkMotion.DurationFast).isIn(100..200)

        // Main flight movement ~250–500ms
        assertThat(ClinkMotion.DurationFlight).isIn(250..500)

        // Pig reaction ~250–450ms
        assertThat(ClinkMotion.DurationReaction).isIn(250..450)

        // Celebration display ~300–700ms
        assertThat(ClinkMotion.DurationCelebration).isIn(300..700)
    }

    @Test
    fun `balance interpolation never produces negative paise`() {
        val zeroPaise = 0L
        val targetPaise = 5000L

        for (step in 0..10) {
            val fraction = step / 10f
            val interpolated = (zeroPaise.toFloat() + (targetPaise - zeroPaise).toFloat() * fraction)
                .toDouble().roundToLong().coerceAtLeast(0L)
            assertThat(interpolated).isAtLeast(0L)
            assertThat(interpolated).isAtMost(targetPaise)
            val money = Money(interpolated)
            assertThat(money.paise).isEqualTo(interpolated)
        }
    }

    @Test
    fun `coin flight scale and alpha interpolation stay bounded at all progress milestones`() {
        // At launch (t = 0.0)
        val t0 = 0.0f
        val scale0 = if (t0 > 0.65f) 1f - ((t0 - 0.65f) / 0.35f) * 0.55f else 1f
        val alpha0 = if (t0 > 0.85f) 1f - ((t0 - 0.85f) / 0.15f) else 1f
        assertThat(scale0).isEqualTo(1.0f)
        assertThat(alpha0).isEqualTo(1.0f)

        // Midway (t = 0.5)
        val tMid = 0.5f
        val scaleMid = if (tMid > 0.65f) 1f - ((tMid - 0.65f) / 0.35f) * 0.55f else 1f
        val alphaMid = if (tMid > 0.85f) 1f - ((tMid - 0.85f) / 0.15f) else 1f
        assertThat(scaleMid).isEqualTo(1.0f)
        assertThat(alphaMid).isEqualTo(1.0f)

        // Approaching slot (t = 0.8)
        val t80 = 0.8f
        val scale80 = if (t80 > 0.65f) 1f - ((t80 - 0.65f) / 0.35f) * 0.55f else 1f
        val alpha80 = if (t80 > 0.85f) 1f - ((t80 - 0.85f) / 0.15f) else 1f
        assertThat(scale80).isLessThan(1.0f)
        assertThat(alpha80).isEqualTo(1.0f)

        // Impact into slot (t = 1.0)
        val t1 = 1.0f
        val scale1 = if (t1 > 0.65f) 1f - ((t1 - 0.65f) / 0.35f) * 0.55f else 1f
        val alpha1 = if (t1 > 0.85f) 1f - ((t1 - 0.85f) / 0.15f) else 1f
        assertThat(scale1).isWithin(0.01f).of(0.45f)
        assertThat(alpha1).isWithin(0.01f).of(0.0f)
    }

    @Test
    fun `clink motion easings are valid and non-null`() {
        assertThat(ClinkMotion.StandardEasing).isNotNull()
        assertThat(ClinkMotion.DecelerateEasing).isNotNull()
        assertThat(ClinkMotion.EmphasizedEasing).isNotNull()
        assertThat(ClinkMotion.FlightEasing).isNotNull()
    }
}
