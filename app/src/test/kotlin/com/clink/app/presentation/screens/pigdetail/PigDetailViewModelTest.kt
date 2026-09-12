package com.clink.app.presentation.screens.pigdetail

import androidx.lifecycle.SavedStateHandle
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.repository.PigRepository
import com.google.common.truth.Truth.assertThat
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
class PigDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockPigRepository: PigRepository
    private val pigFlow = MutableStateFlow<Pig?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPigRepository = mockk(relaxed = true)

        every { mockPigRepository.getPigById(1L) } returns pigFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads pig reactively from repository by savedStateHandle pigId`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("pigId" to "1"))
        val viewModel = PigDetailViewModel(mockPigRepository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pig.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Initial state before emission
        assertThat(viewModel.pig.value).isNull()

        // Emit pig with ₹750 (Healthy state)
        val expectedPig = Pig(
            id = 1L,
            name = "Vacation Pig",
            balance = Money.fromRupees(750)
        )
        pigFlow.value = expectedPig
        testDispatcher.scheduler.advanceUntilIdle()

        val observedPig = viewModel.pig.value
        assertThat(observedPig).isNotNull()
        assertThat(observedPig?.name).isEqualTo("Vacation Pig")
        assertThat(observedPig?.balance).isEqualTo(Money.fromRupees(750))
        assertThat(observedPig?.state).isEqualTo(PigState.HEALTHY)
        assertThat(observedPig?.progression?.state).isEqualTo(PigState.HEALTHY)
    }

    @Test
    fun `handles missing or invalid pigId by defaulting to 1L`() = runTest {
        val savedStateHandle = SavedStateHandle(emptyMap())
        val viewModel = PigDetailViewModel(mockPigRepository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pig.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val defaultPig = Pig(id = 1L, name = "Default", balance = Money.ZERO)
        pigFlow.value = defaultPig
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.pig.value?.id).isEqualTo(1L)
        assertThat(viewModel.pig.value?.state).isEqualTo(PigState.NEW)
    }
}
