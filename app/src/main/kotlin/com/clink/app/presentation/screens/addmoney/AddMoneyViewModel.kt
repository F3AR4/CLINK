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

data class AddMoneyUiState(
    val selectedAmount: Money = Money.RS_10,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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
        _uiState.value = _uiState.value.copy(selectedAmount = amount)
    }

    fun onAddMoney() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = addMoneyUseCase(
                pigId = pigId,
                amount = _uiState.value.selectedAmount,
                note = "Clink savings"
            )
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    _events.emit(AddMoneyEvent.Success)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                    _events.emit(AddMoneyEvent.Error(error.message ?: "Failed to add money"))
                }
            )
        }
    }
}
