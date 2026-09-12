package com.clink.app.domain.model

import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import com.clink.app.domain.usecase.AddMoneyUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * Authoritative financial integrity and boundary value test suite.
 * Enforces zero floating-point arithmetic, strict integer paise boundaries,
 * overflow protection, and transaction atomicity.
 */
class FinancialIntegrityTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)

    private lateinit var addMoneyUseCase: AddMoneyUseCase

    private val basePig = Pig(
        id = 1L,
        name = "Primary Pig",
        balance = Money.ZERO
    )

    @Before
    fun setUp() {
        addMoneyUseCase = AddMoneyUseCase(pigRepository, transactionRepository, paymentRepository)
        coEvery { pigRepository.getPigByIdOnce(1L) } returns basePig
    }

    @Test
    fun `zero amount is rejected at both domain model and use case boundaries`() = runTest {
        // Zero money representation
        val zeroMoney = Money(0L)
        assertThat(zeroMoney.isZero).isTrue()
        assertThat(zeroMoney.isPositive).isFalse()

        // Use case validation
        val result = addMoneyUseCase(pigId = 1L, amount = zeroMoney)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("greater than zero paise")

        // Invariant: zero amount must never trigger payment or repository mutation
        coVerify { paymentRepository wasNot Called }
        coVerify { pigRepository wasNot Called }
    }

    @Test
    fun `negative amount is rejected at construction time`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(-1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Money(-1000L)
        }
    }

    @Test
    fun `minimum valid amount of 1 paise is accepted and processed`() = runTest {
        val minPaise = Money(1L)
        assertThat(minPaise.isPositive).isTrue()

        val expectedTx = Transaction(
            id = 101L,
            pigId = 1L,
            amount = minPaise,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "1 paise micro-save"
        )

        coEvery { paymentRepository.processDeposit(1L, minPaise, any()) } returns
            PaymentResult.Success("REF-MIN", minPaise)
        coEvery { pigRepository.addSavings(1L, minPaise, any()) } returns expectedTx

        val result = addMoneyUseCase(pigId = 1L, amount = minPaise, note = "1 paise micro-save")
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.amount).isEqualTo(minPaise)
    }

    @Test
    fun `maximum micro-saving amount of 1 lakh is accepted`() = runTest {
        val maxAmount = Money.fromRupees(100_000L) // ₹1,00,000 = 10,000,000 paise
        assertThat(maxAmount.paise).isEqualTo(10_000_000L)

        val expectedTx = Transaction(
            id = 102L,
            pigId = 1L,
            amount = maxAmount,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED
        )

        coEvery { paymentRepository.processDeposit(1L, maxAmount, any()) } returns
            PaymentResult.Success("REF-MAX", maxAmount)
        coEvery { pigRepository.addSavings(1L, maxAmount, any()) } returns expectedTx

        val result = addMoneyUseCase(pigId = 1L, amount = maxAmount)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.amount).isEqualTo(maxAmount)
    }

    @Test
    fun `overflow boundary beyond Long MAX_VALUE throws ArithmeticException without mutating state`() = runTest {
        val nearMaxPig = basePig.copy(balance = Money(Long.MAX_VALUE - 100L))
        coEvery { pigRepository.getPigByIdOnce(1L) } returns nearMaxPig

        val overflowAmount = Money(200L) // 200 > 100 remaining capacity

        val result = addMoneyUseCase(pigId = 1L, amount = overflowAmount)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ArithmeticException::class.java)

        // No payment or database record on overflow
        coVerify { paymentRepository wasNot Called }
        coVerify(exactly = 0) { pigRepository.addSavings(1L, overflowAmount, any()) }
    }

    @Test
    fun `sequential saves accumulate balance accurately without precision loss`() {
        var currentBalance = Money.ZERO
        val increments = listOf(
            Money.fromRupees(10), // ₹10
            Money.fromRupees(20), // ₹20
            Money.fromRupees(50), // ₹50
            Money.fromRupees(100), // ₹100
            Money(35L), // ₹0.35
            Money(65L)  // ₹0.65 -> sum = ₹181.00
        )

        for (inc in increments) {
            currentBalance += inc
        }

        // Expected: 1000 + 2000 + 5000 + 10000 + 35 + 65 = 18100 paise = ₹181
        assertThat(currentBalance.paise).isEqualTo(18100L)
        assertThat(currentBalance.formatDisplay()).isEqualTo("₹181")
    }

    @Test
    fun `subtraction of exact amount reduces balance to zero`() {
        val balance = Money.fromRupees(50)
        val remaining = balance - Money.fromRupees(50)
        assertThat(remaining).isEqualTo(Money.ZERO)
        assertThat(remaining.isZero).isTrue()
    }

    @Test
    fun `subtraction exceeding current balance is strictly prevented`() {
        val balance = Money.fromRupees(20)
        assertThrows(IllegalArgumentException::class.java) {
            balance - Money.fromRupees(21)
        }
    }

    @Test
    fun `failed transaction execution completely rolls back without updating pig balance`() = runTest {
        coEvery { paymentRepository.processDeposit(1L, Money.RS_50, any()) } returns
            PaymentResult.Failure("ERR_NET", "Network Timeout")

        val result = addMoneyUseCase(pigId = 1L, amount = Money.RS_50)
        assertThat(result.isFailure).isTrue()

        // Balance must never be incremented on payment failure
        coVerify(exactly = 0) { pigRepository.addSavings(1L, Money.RS_50, any()) }

        // Transaction log must reflect failure for auditability
        coVerify {
            transactionRepository.recordTransaction(
                match { it.status == TransactionStatus.FAILED && it.pigId == 1L && it.amount == Money.RS_50 }
            )
        }
    }
}
