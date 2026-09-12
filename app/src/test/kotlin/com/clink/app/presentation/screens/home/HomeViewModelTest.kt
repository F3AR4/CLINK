package com.clink.app.presentation.screens.home

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockPigRepository: PigRepository
    private lateinit var mockGetTransactionsUseCase: GetTransactionsUseCase
    private val pigsFlow = MutableStateFlow<List<Pig>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPigRepository = mockk(relaxed = true)
        mockGetTransactionsUseCase = mockk(relaxed = true)

        coEvery { mockPigRepository.getAllPigs() } returns pigsFlow
        every { mockGetTransactionsUseCase() } returns transactionsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls getOrCreateDefaultPig to ensure idempotent startup`() = runTest {
        HomeViewModel(mockPigRepository, mockGetTransactionsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockPigRepository.getOrCreateDefaultPig() }
    }

    @Test
    fun `emits primary pig and progression reactively when pigs flow updates`() = runTest {
        val viewModel = HomeViewModel(mockPigRepository, mockGetTransactionsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val initialPig = Pig(id = 1L, name = "Primary Pig", balance = Money.ZERO)
        pigsFlow.value = listOf(initialPig)
        testDispatcher.scheduler.advanceUntilIdle()

        val state1 = viewModel.uiState.value
        assertThat(state1.isLoading).isFalse()
        assertThat(state1.pigs).hasSize(1)
        assertThat(state1.primaryPig).isEqualTo(initialPig)
        assertThat(state1.primaryPig?.state).isEqualTo(PigState.NEW)
        assertThat(state1.totalBalance).isEqualTo(Money.ZERO)

        // Simulate deposit to ₹600 (Healthy tier)
        val healthyPig = initialPig.copy(balance = Money.fromRupees(600))
        pigsFlow.value = listOf(healthyPig)
        testDispatcher.scheduler.advanceUntilIdle()

        val state2 = viewModel.uiState.value
        assertThat(state2.primaryPig?.balance).isEqualTo(Money.fromRupees(600))
        assertThat(state2.primaryPig?.state).isEqualTo(PigState.HEALTHY)
        assertThat(state2.totalBalance).isEqualTo(Money.fromRupees(600))
    }

    @Test
    fun `emits recent transactions limited to maximum 3 items`() = runTest {
        val viewModel = HomeViewModel(mockPigRepository, mockGetTransactionsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val txList = listOf(
            Transaction(id = 4L, pigId = 1L, amount = Money.RS_50, type = TransactionType.CREDIT, timestamp = 4000L),
            Transaction(id = 3L, pigId = 1L, amount = Money.RS_20, type = TransactionType.CREDIT, timestamp = 3000L),
            Transaction(id = 2L, pigId = 1L, amount = Money.RS_10, type = TransactionType.CREDIT, timestamp = 2000L),
            Transaction(id = 1L, pigId = 1L, amount = Money.RS_10, type = TransactionType.CREDIT, timestamp = 1000L)
        )
        transactionsFlow.value = txList
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // Must take at most 3 items
        assertThat(state.recentTransactions).hasSize(3)
        assertThat(state.recentTransactions[0].id).isEqualTo(4L)
        assertThat(state.recentTransactions[1].id).isEqualTo(3L)
        assertThat(state.recentTransactions[2].id).isEqualTo(2L)
    }

    @Test
    fun `handles empty transactions with empty recentTransactions list`() = runTest {
        val viewModel = HomeViewModel(mockPigRepository, mockGetTransactionsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        transactionsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.recentTransactions).isEmpty()
    }

    @Test
    fun `emits full state when pig reaches top savings tier`() = runTest {
        val viewModel = HomeViewModel(mockPigRepository, mockGetTransactionsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val fullPig = Pig(id = 1L, name = "Primary Pig", balance = Money.fromRupees(2500))
        pigsFlow.value = listOf(fullPig)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.primaryPig?.state).isEqualTo(PigState.FULL)
        assertThat(state.primaryPig?.progression?.nextThreshold).isNull()
    }
}
