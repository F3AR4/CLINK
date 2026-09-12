package com.clink.app.presentation.screens.goals

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.usecase.CreateGoalUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateGoalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val createGoalUseCase: CreateGoalUseCase = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf("pigId" to "1"))
    private lateinit var viewModel: CreateGoalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateGoalViewModel(createGoalUseCase, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields, no errors and cannot create`() {
        val state = viewModel.uiState.value
        assertThat(state.title).isEmpty()
        assertThat(state.targetAmountText).isEmpty()
        assertThat(state.titleError).isNull()
        assertThat(state.amountError).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.canCreate).isFalse()
    }

    @Test
    fun `typing title and valid amount enables creation`() {
        viewModel.onTitleChange("New Headphones")
        viewModel.onAmountChange("500")

        val state = viewModel.uiState.value
        assertThat(state.title).isEqualTo("New Headphones")
        assertThat(state.targetAmountText).isEqualTo("500")
        assertThat(state.targetAmount).isEqualTo(Money(50000L)) // ₹500 in paise
        assertThat(state.canCreate).isTrue()
    }

    @Test
    fun `amount input filters out non-digits`() {
        viewModel.onAmountChange("abc 12.5 # 00")
        assertThat(viewModel.uiState.value.targetAmountText).isEqualTo("12500")
        assertThat(viewModel.uiState.value.targetAmount).isEqualTo(Money(1250000L))
    }

    @Test
    fun `typing blank title sets titleError`() {
        viewModel.onTitleChange("   ")
        val state = viewModel.uiState.value
        assertThat(state.titleError).isEqualTo("Enter a goal name")
        assertThat(state.canCreate).isFalse()
    }

    @Test
    fun `entering zero amount sets amountError`() {
        viewModel.onTitleChange("New Shoes")
        viewModel.onAmountChange("0")

        val state = viewModel.uiState.value
        assertThat(state.amountError).isEqualTo("Enter an amount greater than ₹0")
        assertThat(state.canCreate).isFalse()
    }

    @Test
    fun `successful goal creation emits Success event`() = runTest {
        val createdGoal = Goal(
            id = 5L,
            pigId = 1L,
            title = "New Headphones",
            targetAmount = Money(50000L), // ₹500
            createdAt = 1000L
        )
        coEvery {
            createGoalUseCase(pigId = 1L, title = "New Headphones", targetAmount = Money(50000L))
        } returns Result.success(createdGoal)

        viewModel.events.test {
            viewModel.onTitleChange("New Headphones")
            viewModel.onAmountChange("500")
            viewModel.onCreateGoal()

            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isEqualTo(CreateGoalEvent.Success)
            assertThat(viewModel.uiState.value.status).isEqualTo(CreateGoalStatus.Success)
        }
    }

    @Test
    fun `creation failure emits Error event and sets error status`() = runTest {
        coEvery {
            createGoalUseCase(pigId = 1L, title = "Trip", targetAmount = Money(100000L))
        } returns Result.failure(RuntimeException("Database insert error"))

        viewModel.events.test {
            viewModel.onTitleChange("Trip")
            viewModel.onAmountChange("1000")
            viewModel.onCreateGoal()

            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(CreateGoalEvent.Error::class.java)
            assertThat((event as CreateGoalEvent.Error).message).isEqualTo("Database insert error")
            assertThat(viewModel.uiState.value.status).isInstanceOf(CreateGoalStatus.Error::class.java)
        }
    }
}
