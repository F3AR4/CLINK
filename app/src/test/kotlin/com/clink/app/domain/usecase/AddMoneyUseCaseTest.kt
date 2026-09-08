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
    fun `adding valid money calls payment simulation and persists savings atomically`() = runTest {
        val expectedTransaction = Transaction(
            id = 42L,
            pigId = 1L,
            amount = Money.RS_20,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Pocket change savings"
        )
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery {
            paymentRepository.processDeposit(1L, Money.RS_20, PaymentMethod.SIMULATED_UPI)
        } returns PaymentResult.Success("TX-12345", Money.RS_20)
        coEvery {
            pigRepository.addSavings(1L, Money.RS_20, "Pocket change savings")
        } returns expectedTransaction

        val result = useCase(
            pigId = 1L,
            amount = Money.RS_20,
            note = "Pocket change savings"
        )

        assertThat(result.isSuccess).isTrue()
        val tx = result.getOrNull()
        assertThat(tx).isEqualTo(expectedTransaction)

        coVerify { pigRepository.addSavings(1L, Money.RS_20, "Pocket change savings") }
    }

    @Test
    fun `adding all standard predefined amounts succeed`() = runTest {
        val amounts = listOf(Money.RS_10, Money.RS_20, Money.RS_50, Money.RS_100)
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig

        for (amt in amounts) {
            val dummyTx = Transaction(
                id = amt.paise,
                pigId = 1L,
                amount = amt,
                type = TransactionType.CREDIT
            )
            coEvery { paymentRepository.processDeposit(1L, amt, any()) } returns
                PaymentResult.Success("REF-${amt.paise}", amt)
            coEvery { pigRepository.addSavings(1L, amt, any()) } returns dummyTx

            val result = useCase(pigId = 1L, amount = amt)
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()?.amount).isEqualTo(amt)
        }
    }

    @Test
    fun `adding zero money returns failure without calling payment or repository`() = runTest {
        val result = useCase(pigId = 1L, amount = Money.ZERO)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("greater than zero paise")
        coVerify { paymentRepository wasNot Called }
        coVerify { pigRepository wasNot Called }
    }

    @Test
    fun `adding money to non-existent pig returns failure`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(999L) } returns null

        val result = useCase(pigId = 999L, amount = Money.RS_10)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("Pig with ID 999 not found")
        coVerify { paymentRepository wasNot Called }
    }

    @Test
    fun `balance arithmetic overflow returns failure without processing payment`() = runTest {
        val nearMaxPig = testPig.copy(balance = Money(Long.MAX_VALUE - 500L))
        coEvery { pigRepository.getPigByIdOnce(1L) } returns nearMaxPig

        val result = useCase(pigId = 1L, amount = Money.RS_10) // 1000 paise > 500 headroom

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ArithmeticException::class.java)
        coVerify { paymentRepository wasNot Called }
    }

    @Test
    fun `failed payment logs failed transaction and does not update pig balance`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery {
            paymentRepository.processDeposit(1L, Money.RS_50, any())
        } returns PaymentResult.Failure("ERR_SIM", "Bank declined")

        val result = useCase(pigId = 1L, amount = Money.RS_50)

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { pigRepository.addSavings(1L, Money.RS_50, any()) }
        coVerify {
            transactionRepository.recordTransaction(
                match { it.status == TransactionStatus.FAILED && it.pigId == 1L }
            )
        }
    }

    @Test
    fun `repository failure during addSavings returns failure result`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery {
            paymentRepository.processDeposit(1L, Money.RS_10, any())
        } returns PaymentResult.Success("TX-123", Money.RS_10)
        coEvery {
            pigRepository.addSavings(1L, Money.RS_10, any())
        } throws RuntimeException("Database I/O error")

        val result = useCase(pigId = 1L, amount = Money.RS_10)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("Database I/O error")
    }
}
