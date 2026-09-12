package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetTransactionsUseCaseTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var useCase: GetTransactionsUseCase

    private val sampleTx1 = Transaction(
        id = 1L,
        pigId = 10L,
        amount = Money.RS_10,
        type = TransactionType.CREDIT,
        status = TransactionStatus.COMPLETED,
        note = "First save",
        timestamp = 1000L
    )

    private val sampleTx2 = Transaction(
        id = 2L,
        pigId = 20L,
        amount = Money.RS_20,
        type = TransactionType.CREDIT,
        status = TransactionStatus.COMPLETED,
        note = "Second save",
        timestamp = 2000L
    )

    @Before
    fun setUp() {
        mockTransactionRepository = mockk()
        useCase = GetTransactionsUseCase(mockTransactionRepository)
    }

    @Test
    fun `invoke with null pigId returns all transactions from repository`() = runTest {
        every { mockTransactionRepository.getAllTransactions() } returns flowOf(listOf(sampleTx2, sampleTx1))

        val result = useCase(pigId = null).first()

        assertThat(result).containsExactly(sampleTx2, sampleTx1).inOrder()
        verify(exactly = 1) { mockTransactionRepository.getAllTransactions() }
        verify(exactly = 0) { mockTransactionRepository.getTransactionsForPig(any()) }
    }

    @Test
    fun `invoke with specific pigId returns filtered transactions for that pig`() = runTest {
        every { mockTransactionRepository.observeTransactions(10L) } returns flowOf(listOf(sampleTx1))

        val result = useCase(pigId = 10L).first()

        assertThat(result).containsExactly(sampleTx1)
        verify(exactly = 1) { mockTransactionRepository.observeTransactions(10L) }
        verify(exactly = 0) { mockTransactionRepository.getAllTransactions() }
    }
}
