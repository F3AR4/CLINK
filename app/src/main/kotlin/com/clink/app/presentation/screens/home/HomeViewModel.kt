package com.clink.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val pigs: List<Pig> = emptyList(),
    val primaryPig: Pig? = null,
    val totalBalance: Money = Money.ZERO,
    val recentTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pigRepository: PigRepository,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        pigRepository.getAllPigs(),
        getTransactionsUseCase()
    ) { pigs, transactions ->
        val primary = pigs.firstOrNull()
        val totalPaise = primary?.balance?.paise ?: 0L
        HomeUiState(
            isLoading = false,
            pigs = pigs,
            primaryPig = primary,
            totalBalance = Money(totalPaise),
            recentTransactions = transactions.take(3)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        ensureDefaultPig()
    }

    private fun ensureDefaultPig() {
        viewModelScope.launch {
            pigRepository.getOrCreateDefaultPig()
        }
    }
}
