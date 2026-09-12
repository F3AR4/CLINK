package com.clink.app.presentation.screens.history

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Tests transaction history ordering, tie-breaking, grouping, and high-volume synthetic lists.
 */
class TransactionReliabilityTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getTransactionsUseCase: GetTransactionsUseCase = mockk()
    private val pigRepository: PigRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { pigRepository.getPigById(any()) } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `transactions sharing the exact same timestamp are deterministically ordered by id descending`() = runTest {
        val sharedTimestamp = 1700000000000L

        val tx1 = Transaction(id = 1L, pigId = 1L, amount = Money.RS_10, type = TransactionType.CREDIT, timestamp = sharedTimestamp)
        val tx2 = Transaction(id = 2L, pigId = 1L, amount = Money.RS_20, type = TransactionType.CREDIT, timestamp = sharedTimestamp)
        val tx3 = Transaction(id = 3L, pigId = 1L, amount = Money.RS_50, type = TransactionType.CREDIT, timestamp = sharedTimestamp)

        // Simulating DAO ordering: timestamp DESC, id DESC -> [tx3, tx2, tx1]
        val orderedList = listOf(tx3, tx2, tx1)
        every { getTransactionsUseCase(1L) } returns flowOf(orderedList)

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = getTransactionsUseCase,
            savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("pigId" to "1")),
            pigRepository = pigRepository
        )

        val collectJob1 = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.transactions).hasSize(3)
        assertThat(state.transactions[0].transaction.id).isEqualTo(3L)
        assertThat(state.transactions[1].transaction.id).isEqualTo(2L)
        assertThat(state.transactions[2].transaction.id).isEqualTo(1L)
        collectJob1.cancel()
    }

    @Test
    fun `large synthetic transaction history processes and groups without error`() = runTest {
        val count = 150
        val baseTime = System.currentTimeMillis()
        val syntheticList = mutableListOf<Transaction>()

        for (i in 1..count) {
            syntheticList.add(
                Transaction(
                    id = i.toLong(),
                    pigId = 1L,
                    amount = Money.fromRupees(i.toLong()),
                    type = TransactionType.CREDIT,
                    status = TransactionStatus.COMPLETED,
                    note = "Synthetic micro-save #$i",
                    timestamp = baseTime - (i * 3600_000L) // Each 1 hour apart
                )
            )
        }

        every { getTransactionsUseCase(1L) } returns flowOf(syntheticList)

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = getTransactionsUseCase,
            savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("pigId" to "1")),
            pigRepository = pigRepository
        )

        val collectJob2 = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.transactionCount).isEqualTo(count)
        assertThat(state.transactions).hasSize(count)

        // Total saved is sum of 1..150 = 150*151/2 = 11,325 Rupees = 1,132,500 paise
        val expectedPaise = (count.toLong() * (count + 1L) / 2L) * 100L
        assertThat(state.totalSaved.paise).isEqualTo(expectedPaise)

        // Groups must be populated cleanly
        assertThat(state.groupedTransactions.keys).isNotEmpty()
        collectJob2.cancel()
    }
}
