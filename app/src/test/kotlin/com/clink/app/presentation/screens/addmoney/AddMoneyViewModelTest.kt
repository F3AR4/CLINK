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
        assertThat(viewModel.uiState.value.canSave).isTrue()
        assertThat(viewModel.uiState.value.isCustomAmount).isFalse()
        assertThat(viewModel.uiState.value.noteText).isEmpty()
    }

    @Test
    fun `selecting quick amounts updates selectedAmount state and clears custom mode`() {
        viewModel.onSelectAmount(Money.RS_20)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_20)
        assertThat(viewModel.uiState.value.isCustomAmount).isFalse()

        viewModel.onSelectQuickAmount(Money.RS_50)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_50)

        viewModel.onSelectQuickAmount(Money.RS_100)
        assertThat(viewModel.uiState.value.selectedAmount).isEqualTo(Money.RS_100)
    }

    @Test
    fun `entering valid custom amount sets selectedAmount in integer paise and enables saving`() {
        viewModel.onCustomAmountChange("35")
        val state = viewModel.uiState.value
        assertThat(state.isCustomAmount).isTrue()
        assertThat(state.customAmountText).isEqualTo("35")
        assertThat(state.selectedAmount).isEqualTo(Money(3_500L))
        assertThat(state.validationError).isNull()
        assertThat(state.canSave).isTrue()
    }

    @Test
    fun `entering various amounts tests conversion fidelity without floats`() {
        // ₹1 -> 100 paise
        viewModel.onCustomAmountChange("1")
        assertThat(viewModel.uiState.value.selectedAmount.paise).isEqualTo(100L)

        // ₹10 -> 1000 paise
        viewModel.onCustomAmountChange("10")
        assertThat(viewModel.uiState.value.selectedAmount.paise).isEqualTo(1000L)

        // ₹99 -> 9900 paise
        viewModel.onCustomAmountChange("99")
        assertThat(viewModel.uiState.value.selectedAmount.paise).isEqualTo(9900L)

        // ₹500 -> 50000 paise
        viewModel.onCustomAmountChange("500")
        assertThat(viewModel.uiState.value.selectedAmount.paise).isEqualTo(50000L)
    }

    @Test
    fun `entering empty custom amount shows validation error and disables saving`() {
        viewModel.onCustomAmountChange("")
        val state = viewModel.uiState.value
        assertThat(state.isCustomAmount).isTrue()
        assertThat(state.selectedAmount).isEqualTo(Money.ZERO)
        assertThat(state.validationError).isEqualTo("Please enter an amount")
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `entering zero custom amount shows validation error and disables saving`() {
        viewModel.onCustomAmountChange("0")
        val state = viewModel.uiState.value
        assertThat(state.isCustomAmount).isTrue()
        assertThat(state.selectedAmount).isEqualTo(Money.ZERO)
        assertThat(state.validationError).isEqualTo("Amount must be greater than ₹0")
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `entering amount exceeding maximum shows validation error and disables saving`() {
        viewModel.onCustomAmountChange("100001")
        val state = viewModel.uiState.value
        assertThat(state.validationError).isEqualTo("Maximum micro-saving amount is ₹1,00,000")
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `entering non-numeric characters filters them safely`() {
        viewModel.onCustomAmountChange("abc25xyz")
        val state = viewModel.uiState.value
        assertThat(state.customAmountText).isEqualTo("25")
        assertThat(state.selectedAmount).isEqualTo(Money(2_500L))
        assertThat(state.validationError).isNull()
    }

    @Test
    fun `switching back to quick denomination clears custom state`() {
        viewModel.onCustomAmountChange("42")
        assertThat(viewModel.uiState.value.isCustomAmount).isTrue()

        viewModel.onSelectQuickAmount(Money.RS_10)
        val state = viewModel.uiState.value
        assertThat(state.isCustomAmount).isFalse()
        assertThat(state.customAmountText).isEmpty()
        assertThat(state.selectedAmount).isEqualTo(Money.RS_10)
        assertThat(state.validationError).isNull()
        assertThat(state.canSave).isTrue()
    }

    @Test
    fun `note handling caps at 50 characters`() {
        val longNote = "A".repeat(60)
        viewModel.onNoteChange(longNote)
        // should ignore string longer than 50
        assertThat(viewModel.uiState.value.noteText).isEmpty()

        val validNote = "Coffee saved"
        viewModel.onNoteChange(validNote)
        assertThat(viewModel.uiState.value.noteText).isEqualTo("Coffee saved")
    }

    @Test
    fun `successful save updates state to Success with savedAmount and emits Success event`() = runTest {
        val dummyTx = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.RS_20,
            type = TransactionType.CREDIT
        )
        coEvery { addMoneyUseCase(1L, Money.RS_20, "Coffee saved") } returns Result.success(dummyTx)

        viewModel.onSelectQuickAmount(Money.RS_20)
        viewModel.onNoteChange("Coffee saved")

        viewModel.events.test {
            viewModel.onAddMoney()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddMoneyEvent.Success::class.java)
            assertThat((event as AddMoneyEvent.Success).amount).isEqualTo(Money.RS_20)

            val state = viewModel.uiState.value
            assertThat(state.saveStatus).isEqualTo(SaveStatus.Success)
            assertThat(state.savedAmount).isEqualTo(Money.RS_20)
            assertThat(state.isLoading).isFalse()
            assertThat(state.isProcessing).isTrue()
        }
    }

    @Test
    fun `saving with blank note uses default fallback note`() = runTest {
        val dummyTx = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.RS_10,
            type = TransactionType.CREDIT
        )
        coEvery { addMoneyUseCase(1L, Money.RS_10, "Clink savings") } returns Result.success(dummyTx)

        viewModel.onNoteChange("   ") // whitespace note
        viewModel.onAddMoney()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { addMoneyUseCase(1L, Money.RS_10, "Clink savings") }
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
        // Second immediate tap while still processing
        viewModel.onAddMoney()

        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once
        coVerify(exactly = 1) { addMoneyUseCase(1L, Money.RS_10, any()) }
    }

    @Test
    fun `resetState restores clean initial state`() {
        viewModel.onCustomAmountChange("55")
        viewModel.onNoteChange("Saved for movie")

        assertThat(viewModel.uiState.value.isCustomAmount).isTrue()
        assertThat(viewModel.uiState.value.noteText).isEqualTo("Saved for movie")

        viewModel.resetState()

        val state = viewModel.uiState.value
        assertThat(state.selectedAmount).isEqualTo(Money.RS_10)
        assertThat(state.customAmountText).isEmpty()
        assertThat(state.isCustomAmount).isFalse()
        assertThat(state.noteText).isEmpty()
        assertThat(state.validationError).isNull()
        assertThat(state.saveStatus).isEqualTo(SaveStatus.Idle)
    }
}
