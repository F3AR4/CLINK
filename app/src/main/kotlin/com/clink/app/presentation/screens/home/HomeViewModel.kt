package com.clink.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val pigs: List<Pig> = emptyList(),
    val totalBalance: Money = Money.ZERO
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pigRepository: PigRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = pigRepository.getAllPigs()
        .map { pigs ->
            val totalPaise = pigs.sumOf { it.balance.paise }
            HomeUiState(
                isLoading = false,
                pigs = pigs,
                totalBalance = Money(totalPaise)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    init {
        ensureDefaultPig()
    }

    private fun ensureDefaultPig() {
        viewModelScope.launch {
            val pigs = pigRepository.getAllPigs()
            // We can initialize a primary pig if the database is newly created
            launch {
                pigRepository.getAllPigs().collect { currentPigs ->
                    if (currentPigs.isEmpty()) {
                        pigRepository.createPig(
                            Pig(
                                name = "Primary Pig",
                                balance = Money.ZERO,
                                targetAmount = Money.fromRupees(5000)
                            )
                        )
                    }
                }
            }
        }
    }
}
