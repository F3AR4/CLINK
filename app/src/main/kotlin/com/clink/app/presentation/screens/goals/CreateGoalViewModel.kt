package com.clink.app.presentation.screens.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Money
import com.clink.app.domain.usecase.CreateGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreateGoalStatus {
    data object Idle : CreateGoalStatus
    data object Creating : CreateGoalStatus
    data object Success : CreateGoalStatus
    data class Error(val message: String) : CreateGoalStatus
}

data class CreateGoalUiState(
    val title: String = "",
    val targetAmountText: String = "",
    val targetAmount: Money = Money.ZERO,
    val titleError: String? = null,
    val amountError: String? = null,
    val status: CreateGoalStatus = CreateGoalStatus.Idle
) {
    val isLoading: Boolean get() = status is CreateGoalStatus.Creating
    val isProcessing: Boolean get() = status is CreateGoalStatus.Creating || status is CreateGoalStatus.Success
    val canCreate: Boolean get() = !isProcessing && title.isNotBlank() && targetAmount.isPositive && titleError == null && amountError == null
}

sealed interface CreateGoalEvent {
    data object Success : CreateGoalEvent
    data class Error(val message: String) : CreateGoalEvent
}

@HiltViewModel
class CreateGoalViewModel @Inject constructor(
    private val createGoalUseCase: CreateGoalUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getSelectedPigUseCase: com.clink.app.domain.usecase.GetSelectedPigUseCase? = null
) : ViewModel() {

    private val pigId: Long = savedStateHandle.get<String>("pigId")?.toLongOrNull() ?: 1L

    private val _uiState = MutableStateFlow(CreateGoalUiState())
    val uiState: StateFlow<CreateGoalUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateGoalEvent>()
    val events: SharedFlow<CreateGoalEvent> = _events.asSharedFlow()

    fun onTitleChange(rawText: String) {
        if (_uiState.value.isProcessing) return
        if (rawText.length > 50) return

        val error = if (rawText.isNotBlank()) null else "Enter a goal name"
        _uiState.value = _uiState.value.copy(
            title = rawText,
            titleError = error
        )
    }

    fun onAmountChange(rawText: String) {
        if (_uiState.value.isProcessing) return
        val filtered = rawText.filter { it.isDigit() }

        if (filtered.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                targetAmountText = "",
                targetAmount = Money.ZERO,
                amountError = "Enter a target amount"
            )
            return
        }

        val rupees = filtered.toLongOrNull()
        if (rupees == null) {
            _uiState.value = _uiState.value.copy(
                targetAmountText = filtered,
                targetAmount = Money.ZERO,
                amountError = "Invalid target amount"
            )
            return
        }

        if (rupees == 0L) {
            _uiState.value = _uiState.value.copy(
                targetAmountText = filtered,
                targetAmount = Money.ZERO,
                amountError = "Enter an amount greater than ₹0"
            )
        } else if (rupees > 100_000_000L) {
            _uiState.value = _uiState.value.copy(
                targetAmountText = filtered,
                targetAmount = Money.ZERO,
                amountError = "Amount is too large"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                targetAmountText = filtered,
                targetAmount = Money.fromRupees(rupees),
                amountError = null
            )
        }
    }

    fun onCreateGoal() {
        val state = _uiState.value
        if (!state.canCreate) return

        val titleToSave = state.title.trim()
        if (titleToSave.isBlank()) {
            _uiState.value = state.copy(titleError = "Enter a goal name")
            return
        }

        if (!state.targetAmount.isPositive) {
            _uiState.value = state.copy(amountError = "Enter an amount greater than ₹0")
            return
        }

        _uiState.value = state.copy(status = CreateGoalStatus.Creating)

        viewModelScope.launch {
            val targetPigId = savedStateHandle.get<String>("pigId")?.toLongOrNull()
                ?: getSelectedPigUseCase?.invoke()?.firstOrNull()?.id
                ?: pigId

            val result = createGoalUseCase(
                pigId = targetPigId,
                title = titleToSave,
                targetAmount = state.targetAmount
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(status = CreateGoalStatus.Success)
                    _events.emit(CreateGoalEvent.Success)
                },
                onFailure = { error ->
                    val message = error.message ?: "Failed to create goal"
                    _uiState.value = _uiState.value.copy(status = CreateGoalStatus.Error(message))
                    _events.emit(CreateGoalEvent.Error(message))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = CreateGoalUiState()
    }
}
