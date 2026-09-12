package com.clink.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.usecase.GetOnboardingStateUseCase
import com.clink.app.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Root UI state determining startup destination and initial loading.
 */
sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val startDestination: String) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    getOnboardingStateUseCase: GetOnboardingStateUseCase
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = getOnboardingStateUseCase()
        .map { isCompleted ->
            val destination = if (isCompleted) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }
            MainUiState.Ready(startDestination = destination) as MainUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState.Loading
        )
}
