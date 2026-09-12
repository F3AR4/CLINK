package com.clink.app.presentation.screens.onboarding

import com.clink.app.domain.usecase.CompleteOnboardingUseCase
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val completeOnboardingUseCase = mockk<CompleteOnboardingUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has isSubmitting false and null error`() {
        val viewModel = OnboardingViewModel(completeOnboardingUseCase)

        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `onCompleteOnboarding success invokes onSuccess callback`() = runTest(testDispatcher) {
        coEvery { completeOnboardingUseCase() } returns Result.success(Unit)
        val viewModel = OnboardingViewModel(completeOnboardingUseCase)

        var onSuccessCalled = false
        viewModel.onCompleteOnboarding {
            onSuccessCalled = true
        }

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(onSuccessCalled).isTrue()
        coVerify(exactly = 1) { completeOnboardingUseCase() }
    }

    @Test
    fun `onCompleteOnboarding failure updates error message and resets isSubmitting`() = runTest(testDispatcher) {
        coEvery { completeOnboardingUseCase() } returns Result.failure(IOException("Write failed"))
        val viewModel = OnboardingViewModel(completeOnboardingUseCase)

        var onSuccessCalled = false
        viewModel.onCompleteOnboarding {
            onSuccessCalled = true
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(onSuccessCalled).isFalse()
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    @Test
    fun `onCompleteOnboarding suppresses duplicate rapid clicks`() = runTest(testDispatcher) {
        coEvery { completeOnboardingUseCase() } returns Result.success(Unit)
        val viewModel = OnboardingViewModel(completeOnboardingUseCase)

        var callCount = 0
        // Rapid fire taps before scheduler advances
        viewModel.onCompleteOnboarding { callCount++ }
        viewModel.onCompleteOnboarding { callCount++ }
        viewModel.onCompleteOnboarding { callCount++ }

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(callCount).isEqualTo(1)
        coVerify(exactly = 1) { completeOnboardingUseCase() }
    }

    @Test
    fun `onDismissError clears the errorMessage`() = runTest(testDispatcher) {
        coEvery { completeOnboardingUseCase() } returns Result.failure(IOException("Fail"))
        val viewModel = OnboardingViewModel(completeOnboardingUseCase)

        viewModel.onCompleteOnboarding {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.onDismissError()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
