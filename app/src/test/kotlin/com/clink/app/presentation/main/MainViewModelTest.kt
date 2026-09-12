package com.clink.app.presentation.main

import com.clink.app.domain.usecase.GetOnboardingStateUseCase
import com.clink.app.presentation.navigation.Screen
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getOnboardingStateUseCase = mockk<GetOnboardingStateUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading before upstream flow produces value`() {
        val flow = MutableSharedFlow<Boolean>()
        every { getOnboardingStateUseCase() } returns flow

        val viewModel = MainViewModel(getOnboardingStateUseCase)
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Loading)
    }

    @Test
    fun `routes to Onboarding when onboarding is incomplete`() = runTest(testDispatcher) {
        val flow = MutableStateFlow(false)
        every { getOnboardingStateUseCase() } returns flow

        val viewModel = MainViewModel(getOnboardingStateUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Ready(startDestination = Screen.Onboarding.route))
    }

    @Test
    fun `routes directly to Home when onboarding is already complete`() = runTest(testDispatcher) {
        val flow = MutableStateFlow(true)
        every { getOnboardingStateUseCase() } returns flow

        val viewModel = MainViewModel(getOnboardingStateUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Ready(startDestination = Screen.Home.route))
    }
}
