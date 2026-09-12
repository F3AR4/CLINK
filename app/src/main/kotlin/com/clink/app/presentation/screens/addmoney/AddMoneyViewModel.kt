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
    val saveStatus: SaveStatus = SaveStatus.Idle
) {
    val isLoading: Boolean get() = saveStatus is SaveStatus.Saving
    val isProcessing: Boolean get() = saveStatus is SaveStatus.Saving || saveStatus is SaveStatus.Success
    val errorMessage: String? get() = (saveStatus as? SaveStatus.Error)?.message
}

sealed interface AddMoneyEvent {
    data object Success : AddMoneyEvent
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
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(selectedAmount = amount)
    }

    fun onAddMoney() {
        // Prevent duplicate rapid save taps while already processing or after success
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Saving)

        viewModelScope.launch {
            val result = addMoneyUseCase(
                pigId = pigId,
                amount = _uiState.value.selectedAmount,
                note = "Clink savings"
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Success)
                    _events.emit(AddMoneyEvent.Success)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Failed to add money"
                    _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Error(msg))
                    _events.emit(AddMoneyEvent.Error(msg))
                }
            )
        }
    }
}
