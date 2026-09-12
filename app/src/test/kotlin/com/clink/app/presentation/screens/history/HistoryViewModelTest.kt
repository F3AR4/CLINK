package com.clink.app.presentation.screens.history

import androidx.lifecycle.SavedStateHandle
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockGetTransactionsUseCase: GetTransactionsUseCase

    private val now = System.currentTimeMillis()
    private val oneDayAgo = now - 86_400_000L

    private val testTransactions = listOf(
        Transaction(
            id = 10L,
            pigId = 1L,
            amount = Money.RS_50,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Newest save",
            timestamp = now
        ),
        Transaction(
            id = 9L,
            pigId = 1L,
            amount = Money.RS_20,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Earlier save",
            timestamp = oneDayAgo
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockGetTransactionsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is loading`() = runTest {
        val flow = MutableStateFlow<List<Transaction>>(emptyList())
        every { mockGetTransactionsUseCase(pigId = null) } returns flow

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = SavedStateHandle()
        )

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.totalSaved).isEqualTo(Money.zero)
        assertThat(viewModel.uiState.value.transactionCount).isEqualTo(0)
    }

    @Test
    fun `uiState emits loaded transactions, calculates aggregate totalSaved, transactionCount, and groups them`() = runTest {
        val savedStateHandle = SavedStateHandle()
        every { mockGetTransactionsUseCase(pigId = null) } returns flowOf(testTransactions)

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = savedStateHandle
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.transactionCount).isEqualTo(2)
        assertThat(state.totalSaved).isEqualTo(Money.fromRupees(70))
        assertThat(state.errorMessage).isNull()
        assertThat(state.transactions.map { it.transaction }).containsExactlyElementsIn(testTransactions).inOrder()

        // Verify grouping
        assertThat(state.groupedTransactions).isNotEmpty()
        val allGroupedItems = state.groupedTransactions.values.flatten()
        assertThat(allGroupedItems.map { it.transaction }).containsExactlyElementsIn(testTransactions).inOrder()
    }

    @Test
    fun `uiState with empty list emits isLoading false, totalSaved zero, count zero`() = runTest {
        val savedStateHandle = SavedStateHandle()
        every { mockGetTransactionsUseCase(pigId = null) } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = savedStateHandle
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.transactionCount).isEqualTo(0)
        assertThat(state.totalSaved).isEqualTo(Money.zero)
        assertThat(state.transactions).isEmpty()
        assertThat(state.groupedTransactions).isEmpty()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `transactions convenience flow emits raw domain transactions in order`() = runTest {
        val savedStateHandle = SavedStateHandle()
        every { mockGetTransactionsUseCase(pigId = null) } returns flowOf(testTransactions)

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = savedStateHandle
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect()
        }

        advanceUntilIdle()

        assertThat(viewModel.transactions.value).containsExactlyElementsIn(testTransactions).inOrder()
    }

    @Test
    fun `transactions StateFlow passes pigId from SavedStateHandle to GetTransactionsUseCase`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("pigId" to "1"))
        every { mockGetTransactionsUseCase(pigId = 1L) } returns flowOf(testTransactions)

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = savedStateHandle
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.transactions.map { it.transaction }).containsExactlyElementsIn(testTransactions).inOrder()
    }

    @Test
    fun `repository failure maps to errorMessage in uiState and sets isLoading false`() = runTest {
        every { mockGetTransactionsUseCase(pigId = null) } returns flow {
            throw IOException("Database read failed")
        }

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isEqualTo("Database read failed")
        assertThat(state.transactions).isEmpty()
    }

    @Test
    fun `retry recovers from error when subsequent emission succeeds`() = runTest {
        var callCount = 0
        every { mockGetTransactionsUseCase(pigId = null) } answers {
            flow {
                if (callCount == 0) {
                    callCount++
                    throw IOException("Network timeout")
                } else {
                    emit(testTransactions)
                }
            }
        }

        val viewModel = HistoryViewModel(
            getTransactionsUseCase = mockGetTransactionsUseCase,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("Network timeout")

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.transactionCount).isEqualTo(2)
        assertThat(state.totalSaved).isEqualTo(Money.fromRupees(70))
    }
}
