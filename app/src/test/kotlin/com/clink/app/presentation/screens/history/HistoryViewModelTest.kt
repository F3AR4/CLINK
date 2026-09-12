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

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockGetTransactionsUseCase: GetTransactionsUseCase

    private val testTransactions = listOf(
        Transaction(
            id = 10L,
            pigId = 1L,
            amount = Money.RS_50,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Newest save",
            timestamp = 2000L
        ),
        Transaction(
            id = 9L,
            pigId = 1L,
            amount = Money.RS_20,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Earlier save",
            timestamp = 1000L
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
    fun `transactions StateFlow emits transactions loaded from GetTransactionsUseCase`() = runTest {
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
            viewModel.transactions.collect()
        }

        advanceUntilIdle()

        assertThat(viewModel.transactions.value).containsExactlyElementsIn(testTransactions).inOrder()
    }
}
