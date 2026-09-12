package com.clink.app.presentation.screens.addmoney

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Money
import com.clink.app.domain.usecase.AddMoneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SaveStatus {
    data object Idle : SaveStatus
    data object Saving : SaveStatus
    data object Success : SaveStatus
    data class Error(val message: String) : SaveStatus
}

data class AddMoneyUiState(
    val selectedAmount: Money = Money.RS_10,
    val customAmountText: String = "",
    val isCustomAmount: Boolean = false,
    val noteText: String = "",
    val validationError: String? = null,
    val savedAmount: Money? = null,
    val saveStatus: SaveStatus = SaveStatus.Idle
) {
    val isLoading: Boolean get() = saveStatus is SaveStatus.Saving
    val isProcessing: Boolean get() = saveStatus is SaveStatus.Saving || saveStatus is SaveStatus.Success
    val canSave: Boolean get() = !isProcessing && validationError == null && selectedAmount.isPositive
    val errorMessage: String? get() = (saveStatus as? SaveStatus.Error)?.message
}

sealed interface AddMoneyEvent {
    data class Success(val amount: Money = Money.RS_10) : AddMoneyEvent
    data class Error(val message: String) : AddMoneyEvent
}

@HiltViewModel
class AddMoneyViewModel @Inject constructor(
    private val addMoneyUseCase: AddMoneyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pigId: Long = savedStateHandle.get<String>("pigId")?.toLongOrNull() ?: 1L

    private val _uiState = MutableStateFlow(AddMoneyUiState())
    val uiState: StateFlow<AddMoneyUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddMoneyEvent>()
    val events: SharedFlow<AddMoneyEvent> = _events.asSharedFlow()

    fun onSelectAmount(amount: Money) {
        onSelectQuickAmount(amount)
    }

    fun onSelectQuickAmount(amount: Money) {
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(
            selectedAmount = amount,
            customAmountText = "",
            isCustomAmount = false,
            validationError = null
        )
    }

    fun onCustomAmountChange(rawText: String) {
        if (_uiState.value.isProcessing) return

        // Filter for numeric characters only - zero floating-point arithmetic
        val filtered = rawText.filter { it.isDigit() }

        if (filtered.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                customAmountText = "",
                isCustomAmount = true,
                selectedAmount = Money.ZERO,
                validationError = "Please enter an amount"
            )
            return
        }

        val rupees = filtered.toLongOrNull()
        if (rupees == null) {
            _uiState.value = _uiState.value.copy(
                customAmountText = filtered,
                isCustomAmount = true,
                selectedAmount = Money.ZERO,
                validationError = "Invalid amount"
            )
            return
        }

        if (rupees == 0L) {
            _uiState.value = _uiState.value.copy(
                customAmountText = filtered,
                isCustomAmount = true,
                selectedAmount = Money.ZERO,
                validationError = "Amount must be greater than ₹0"
            )
        } else if (rupees > 100_000L) {
            _uiState.value = _uiState.value.copy(
                customAmountText = filtered,
                isCustomAmount = true,
                selectedAmount = Money.fromRupees(rupees),
                validationError = "Maximum micro-saving amount is ₹1,00,000"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                customAmountText = filtered,
                isCustomAmount = true,
                selectedAmount = Money.fromRupees(rupees),
                validationError = null
            )
        }
    }

    fun onNoteChange(text: String) {
        if (_uiState.value.isProcessing) return
        if (text.length <= 50) {
            _uiState.value = _uiState.value.copy(noteText = text)
        }
    }

    fun onAddMoney() {
        // Prevent duplicate rapid save taps while already processing or after success
        if (!_uiState.value.canSave) return

        val amountToSave = _uiState.value.selectedAmount
        val noteToSave = _uiState.value.noteText.trim().ifBlank { "Clink savings" }

        _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Saving)

        viewModelScope.launch {
            val result = addMoneyUseCase(
                pigId = pigId,
                amount = amountToSave,
                note = noteToSave
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        saveStatus = SaveStatus.Success,
                        savedAmount = amountToSave
                    )
                    _events.emit(AddMoneyEvent.Success(amountToSave))
                },
                onFailure = { error ->
                    val msg = when (error) {
                        is IllegalArgumentException -> error.message ?: "Invalid amount entered"
                        is ArithmeticException -> "Amount exceeds allowable limit"
                        else -> error.message ?: "Failed to save money. Please try again."
                    }
                    _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Error(msg))
                    _events.emit(AddMoneyEvent.Error(msg))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = AddMoneyUiState()
    }
}
