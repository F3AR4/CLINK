package com.clink.app.presentation.screens.home

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import com.clink.app.domain.usecase.CreatePigUseCase
import com.clink.app.domain.usecase.DeletePigUseCase
import com.clink.app.domain.usecase.GetSelectedPigUseCase
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
import com.clink.app.domain.usecase.SelectPigUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Reliability test for active Pig selection, persistence, fallback, and rapid switching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PigSelectionReliabilityTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val getTransactionsUseCase: GetTransactionsUseCase = mockk(relaxed = true)
    private val observeGoalsUseCase: ObserveGoalsUseCase = mockk(relaxed = true)

    private val selectedPigIdFlow = MutableStateFlow<Long?>(null)
    private val allPigsFlow = MutableStateFlow<List<Pig>>(emptyList())

    private lateinit var getSelectedPigUseCase: GetSelectedPigUseCase
    private lateinit var selectPigUseCase: SelectPigUseCase
    private lateinit var createPigUseCase: CreatePigUseCase
    private lateinit var deletePigUseCase: DeletePigUseCase

    private val pig1 = Pig(id = 1L, name = "Primary Pig", balance = Money.RS_100)
    private val pig2 = Pig(id = 2L, name = "Emergency Fund", balance = Money.RS_50)
    private val pig3 = Pig(id = 3L, name = "Travel Pot", balance = Money.RS_20)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        allPigsFlow.value = listOf(pig1, pig2, pig3)
        every { pigRepository.getAllPigs() } returns allPigsFlow
        every { userPreferencesRepository.selectedPigId } returns selectedPigIdFlow
        coEvery { userPreferencesRepository.setSelectedPigId(any()) } answers {
            selectedPigIdFlow.value = firstArg()
            Result.success(Unit)
        }
        every { getTransactionsUseCase(any()) } returns flowOf(emptyList())
        every { getTransactionsUseCase() } returns flowOf(emptyList())
        every { observeGoalsUseCase(any()) } returns flowOf(emptyList())
        every { observeGoalsUseCase() } returns flowOf(emptyList())

        getSelectedPigUseCase = GetSelectedPigUseCase(pigRepository, userPreferencesRepository)
        selectPigUseCase = SelectPigUseCase(pigRepository, userPreferencesRepository)
        createPigUseCase = CreatePigUseCase(pigRepository, userPreferencesRepository)
        deletePigUseCase = DeletePigUseCase(pigRepository, userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when selectedPigId is null, GetSelectedPigUseCase automatically selects first pig`() = runTest {
        selectedPigIdFlow.value = null
        val selectedPig = getSelectedPigUseCase().first()
        assertThat(selectedPig?.id).isEqualTo(1L)
        assertThat(selectedPig?.name).isEqualTo("Primary Pig")
    }

    @Test
    fun `explicit selection immediately updates active selection`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(2L) } returns pig2

        val result = selectPigUseCase(2L)
        assertThat(result.isSuccess).isTrue()
        assertThat(selectedPigIdFlow.value).isEqualTo(2L)

        val active = getSelectedPigUseCase().first()
        assertThat(active?.id).isEqualTo(2L)
        assertThat(active?.name).isEqualTo("Emergency Fund")
    }

    @Test
    fun `rapid switching between pigs updates selectedPigId deterministically`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns pig1
        coEvery { pigRepository.getPigByIdOnce(2L) } returns pig2
        coEvery { pigRepository.getPigByIdOnce(3L) } returns pig3

        selectPigUseCase(1L)
        selectPigUseCase(2L)
        selectPigUseCase(3L)
        selectPigUseCase(2L)

        assertThat(selectedPigIdFlow.value).isEqualTo(2L)
        val active = getSelectedPigUseCase().first()
        assertThat(active?.id).isEqualTo(2L)
    }

    @Test
    fun `process restart simulation with saved selection restores active pig`() = runTest {
        // Preference repository already has 3L persisted from prior session
        selectedPigIdFlow.value = 3L

        // Fresh GetSelectedPigUseCase initialized in new session
        val freshUseCase = GetSelectedPigUseCase(pigRepository, userPreferencesRepository)
        val restoredPig = freshUseCase().first()

        assertThat(restoredPig?.id).isEqualTo(3L)
        assertThat(restoredPig?.name).isEqualTo("Travel Pot")
    }

    @Test
    fun `HomeViewModel reactively updates active pig and balances when selection changes`() = runTest {
        val viewModel = HomeViewModel(
            pigRepository = pigRepository,
            getTransactionsUseCase = getTransactionsUseCase,
            observeGoalsUseCase = observeGoalsUseCase,
            getSelectedPigUseCase = getSelectedPigUseCase,
            selectPigUseCase = selectPigUseCase,
            createPigUseCase = createPigUseCase
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // Initially pig 1 (default)
        assertThat(viewModel.uiState.value.selectedPig?.id).isEqualTo(1L)
        assertThat(viewModel.uiState.value.totalBalance).isEqualTo(Money.RS_100)

        // Select pig 2
        coEvery { pigRepository.getPigByIdOnce(2L) } returns pig2
        viewModel.onSelectPig(2L)
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedPig?.id).isEqualTo(2L)
        assertThat(viewModel.uiState.value.totalBalance).isEqualTo(Money.RS_50)

        collectJob.cancel()
    }
}
