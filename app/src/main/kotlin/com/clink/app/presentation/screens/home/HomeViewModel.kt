package com.clink.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
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
    val recentTransactions: List<Transaction> = emptyList(),
    val activeGoal: GoalProgress? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pigRepository: PigRepository,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val observeGoalsUseCase: ObserveGoalsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        pigRepository.getAllPigs(),
        getTransactionsUseCase(),
        observeGoalsUseCase()
    ) { pigs, transactions, goals ->
        val primary = pigs.firstOrNull()
        val totalPaise = primary?.balance?.paise ?: 0L
        val activeGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()
        HomeUiState(
            isLoading = false,
            pigs = pigs,
            primaryPig = primary,
            totalBalance = Money(totalPaise),
            recentTransactions = transactions.take(3),
            activeGoal = activeGoal
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
