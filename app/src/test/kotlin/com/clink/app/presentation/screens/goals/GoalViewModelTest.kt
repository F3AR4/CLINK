package com.clink.app.presentation.screens.goals

import app.cash.turbine.test
import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.model.Money
import com.clink.app.domain.usecase.DeleteGoalUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeGoalsUseCase: ObserveGoalsUseCase = mockk()
    private val deleteGoalUseCase: DeleteGoalUseCase = mockk()

    private val testGoal = Goal(
        id = 1L,
        pigId = 1L,
        title = "New Headphones",
        targetAmount = Money(500000L),
        createdAt = 1000L
    )

    private val testGoalProgress = GoalProgress(
        goal = testGoal,
        currentAmount = Money(125000L),
        targetAmount = Money(500000L),
        remainingAmount = Money(375000L),
        progressPercent = 25,
        progressFraction = 0.25f,
        isCompleted = false
    )

    private val goalsFlow = MutableStateFlow<List<GoalProgress>>(emptyList())
    private lateinit var viewModel: GoalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeGoalsUseCase() } returns goalsFlow
        viewModel = GoalViewModel(observeGoalsUseCase, deleteGoalUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observes goals flow reactively`() = runTest {
        viewModel.goals.test {
            assertThat(awaitItem()).isEmpty()

            goalsFlow.value = listOf(testGoalProgress)
            assertThat(awaitItem()).containsExactly(testGoalProgress)
        }
    }

    @Test
    fun `deleteGoal calls deleteGoalUseCase and completes successfully`() = runTest {
        coEvery { deleteGoalUseCase(1L) } returns Result.success(Unit)

        viewModel.deleteGoal(1L)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteGoalUseCase(1L) }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `deleteGoal failure updates errorMessage`() = runTest {
        coEvery { deleteGoalUseCase(1L) } returns Result.failure(RuntimeException("Deletion failed"))

        viewModel.deleteGoal(1L)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteGoalUseCase(1L) }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("Deletion failed")
    }

    @Test
    fun `clearError clears error message from state`() = runTest {
        coEvery { deleteGoalUseCase(1L) } returns Result.failure(RuntimeException("Failed"))
        viewModel.deleteGoal(1L)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.clearError()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
