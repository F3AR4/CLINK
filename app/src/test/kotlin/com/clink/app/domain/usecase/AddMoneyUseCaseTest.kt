package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddMoneyUseCaseTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)

    private lateinit var useCase: AddMoneyUseCase

    private val testPig = Pig(
        id = 1L,
        name = "Test Pig",
        balance = Money.RS_10
    )

    @Before
    fun setUp() {
        useCase = AddMoneyUseCase(pigRepository, transactionRepository, paymentRepository)
    }

    @Test
    fun `adding valid money updates pig balance and records transaction`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery {
            paymentRepository.processDeposit(1L, Money.RS_20, PaymentMethod.SIMULATED_UPI)
        } returns PaymentResult.Success("TX-12345", Money.RS_20)
        coEvery { transactionRepository.recordTransaction(any()) } returns 42L

        val result = useCase(
            pigId = 1L,
            amount = Money.RS_20,
            note = "Pocket change savings"
        )

        assertThat(result.isSuccess).isTrue()
        val tx = result.getOrNull()
        assertThat(tx).isNotNull()
        assertThat(tx?.id).isEqualTo(42L)
        assertThat(tx?.amount).isEqualTo(Money.RS_20)
        assertThat(tx?.type).isEqualTo(TransactionType.CREDIT)
        assertThat(tx?.status).isEqualTo(TransactionStatus.COMPLETED)

        // Verifies balance increased by 2000 paise (1000 + 2000 = 3000 paise)
        coVerify { pigRepository.updateBalance(1L, Money(3000L)) }
        coVerify { transactionRepository.recordTransaction(any()) }
    }

    @Test
    fun `adding zero or negative money returns failure without calling payment`() = runTest {
        val result = useCase(pigId = 1L, amount = Money.ZERO)

        assertThat(result.isFailure).isTrue()
        coVerify { paymentRepository wasNot Called }
        coVerify { pigRepository wasNot Called }
    }

    @Test
    fun `failed payment logs failed transaction and does not update pig balance`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery {
            paymentRepository.processDeposit(1L, Money.RS_50, any())
        } returns PaymentResult.Failure("ERR_SIM", "Bank declined")

        val result = useCase(pigId = 1L, amount = Money.RS_50)

        assertThat(result.isFailure).isTrue()
        // Pig balance must NOT be updated
        coVerify(exactly = 0) { pigRepository.updateBalance(1L, Money(6000L)) }
        // Failed transaction record MUST be created for audit trail
        coVerify {
            transactionRepository.recordTransaction(
                match { it.status == TransactionStatus.FAILED }
            )
        }
    }
}
