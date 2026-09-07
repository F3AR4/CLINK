package com.clink.app.data.repository

import com.clink.app.domain.model.Money
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakePaymentRepositoryTest {

    private lateinit var fakePaymentRepository: FakePaymentRepository

    @Before
    fun setUp() {
        fakePaymentRepository = FakePaymentRepository()
    }

    @Test
    fun `processDeposit succeeds by default with reference and amount`() = runTest {
        val result = fakePaymentRepository.processDeposit(
            pigId = 1L,
            amount = Money.RS_50,
            paymentMethod = PaymentMethod.SIMULATED_UPI
        )

        assertThat(result).isInstanceOf(PaymentResult.Success::class.java)
        val success = result as PaymentResult.Success
        assertThat(success.amount).isEqualTo(Money.RS_50)
        assertThat(success.transactionReference).startsWith("MOCK-DEP-")
    }

    @Test
    fun `processWithdrawal succeeds by default`() = runTest {
        val result = fakePaymentRepository.processWithdrawal(
            pigId = 1L,
            amount = Money.RS_20
        )

        assertThat(result).isInstanceOf(PaymentResult.Success::class.java)
        val success = result as PaymentResult.Success
        assertThat(success.amount).isEqualTo(Money.RS_20)
        assertThat(success.transactionReference).startsWith("MOCK-WDR-")
    }

    @Test
    fun `processDeposit handles failure toggle accurately`() = runTest {
        fakePaymentRepository.shouldFailNext = true
        fakePaymentRepository.failureReason = "Network timeout simulation"

        val result = fakePaymentRepository.processDeposit(
            pigId = 1L,
            amount = Money.RS_100,
            paymentMethod = PaymentMethod.SIMULATED_CASH
        )

        assertThat(result).isInstanceOf(PaymentResult.Failure::class.java)
        val failure = result as PaymentResult.Failure
        assertThat(failure.errorCode).isEqualTo("SIMULATED_ERR_001")
        assertThat(failure.errorMessage).isEqualTo("Network timeout simulation")

        // Next call should succeed (one-off failure reset)
        val nextResult = fakePaymentRepository.processDeposit(
            pigId = 1L,
            amount = Money.RS_10,
            paymentMethod = PaymentMethod.SIMULATED_UPI
        )
        assertThat(nextResult).isInstanceOf(PaymentResult.Success::class.java)
    }
}
