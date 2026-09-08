package com.clink.app.presentation.screens.addmoney

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.usecase.AddMoneyUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddMoneyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val addMoneyUseCase: AddMoneyUseCase = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf("pigId" to "1"))

    private lateinit var viewModel: AddMoneyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddMoneyViewModel(addMoneyUseCase, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default selected amount is 10 rupees and status is Idle`() {
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_10)
        assertThat(viewModel.uiState.value.saveStatus).isEqualTo(SaveStatus.Idle)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `selecting amounts updates selectedAmount state`() {
        viewModel.onSelectAmount(Money.RS_20)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_20)

        viewModel.onSelectAmount(Money.RS_50)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_50)

        viewModel.onSelectAmount(Money.RS_100)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_100)
    }

    @Test
    fun `successful save updates state to Success and emits Success event`() = runTest {
        val dummyTx = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.RS_10,
            type = TransactionType.CREDIT
        )
        coEvery { addMoneyUseCase(1L, Money.RS_10, any()) } returns Result.success(dummyTx)

        viewModel.events.test {
            viewModel.onAddMoney()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddMoneyEvent.Success::class.java)
            assertThat(viewModel.uiState.value.saveStatus).isEqualTo(SaveStatus.Success)
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }
    }

    @Test
    fun `failed save updates state to Error and emits Error event`() = runTest {
        coEvery {
            addMoneyUseCase(1L, Money.RS_10, any())
        } returns Result.failure(IllegalStateException("Simulated network/DB error"))

        viewModel.events.test {
            viewModel.onAddMoney()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddMoneyEvent.Error::class.java)
            val errorEvent = event as AddMoneyEvent.Error
            assertThat(errorEvent.message).isEqualTo("Simulated network/DB error")

            assertThat(viewModel.uiState.value.saveStatus).isInstanceOf(SaveStatus.Error::class.java)
            assertThat(viewModel.uiState.value.errorMessage).isEqualTo("Simulated network/DB error")
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }
    }

    @Test
    fun `rapid double tap on save does not trigger duplicate save operations`() = runTest {
        val dummyTx = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.RS_10,
            type = TransactionType.CREDIT
        )
        coEvery { addMoneyUseCase(1L, Money.RS_10, any()) } returns Result.success(dummyTx)

        // First tap initiates save
        viewModel.onAddMoney()
        // Second immediate tap while still processing (before scheduler advances)
        viewModel.onAddMoney()

        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once
        coVerify(exactly = 1) { addMoneyUseCase(1L, Money.RS_10, any()) }
    }
}
