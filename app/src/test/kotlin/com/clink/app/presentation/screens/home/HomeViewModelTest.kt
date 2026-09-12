package com.clink.app.presentation.screens.home

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
    private lateinit var mockTxRepository: TransactionRepository
    private val pigsFlow = MutableStateFlow<List<Pig>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPigRepository = mockk(relaxed = true)
        mockTxRepository = mockk(relaxed = true)

        coEvery { mockPigRepository.getAllPigs() } returns pigsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls getOrCreateDefaultPig to ensure idempotent startup`() = runTest {
        HomeViewModel(mockPigRepository, mockTxRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockPigRepository.getOrCreateDefaultPig() }
    }

    @Test
    fun `emits primary pig and progression reactively when pigs flow updates`() = runTest {
        val viewModel = HomeViewModel(mockPigRepository, mockTxRepository)
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
}
