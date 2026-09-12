package com.clink.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onCompleteOnboarding(onSuccess: () -> Unit) {
        // Synchronous double-tap guard
        if (_uiState.value.isSubmitting) return

        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            val result = completeOnboardingUseCase()
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Unable to save setup. Please try again."
                )
            }
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
