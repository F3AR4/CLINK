package com.clink.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.usecase.CreatePigUseCase
import com.clink.app.domain.usecase.GetSelectedPigUseCase
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
import com.clink.app.domain.usecase.SelectPigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val pigs: List<Pig> = emptyList(),
    val selectedPig: Pig? = null,
    val primaryPig: Pig? = null,
    val totalBalance: Money = Money.ZERO,
    val recentTransactions: List<Transaction> = emptyList(),
    val activeGoal: GoalProgress? = null
) {
    val allPigs: List<Pig> get() = pigs
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pigRepository: PigRepository,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val observeGoalsUseCase: ObserveGoalsUseCase,
    private val getSelectedPigUseCase: GetSelectedPigUseCase? = null,
    private val selectPigUseCase: SelectPigUseCase? = null,
    private val createPigUseCase: CreatePigUseCase? = null
) : ViewModel() {

    private val activePigFlow = getSelectedPigUseCase?.invoke()
        ?: pigRepository.getAllPigs().map { it.firstOrNull() }

    val uiState: StateFlow<HomeUiState> = combine(
        pigRepository.getAllPigs(),
        activePigFlow
    ) { pigs, activePig ->
        pigs to activePig
    }.flatMapLatest { (pigs, activePig) ->
        val targetPig = activePig ?: pigs.firstOrNull()
        val pigId = targetPig?.id
        combine(
            if (pigId != null) getTransactionsUseCase(pigId) else getTransactionsUseCase(),
            if (pigId != null) observeGoalsUseCase(pigId) else observeGoalsUseCase()
        ) { transactions, goals ->
            val activeGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()
            HomeUiState(
                isLoading = false,
                pigs = pigs,
                selectedPig = targetPig,
                primaryPig = targetPig,
                totalBalance = targetPig?.balance ?: Money.ZERO,
                recentTransactions = transactions.take(3),
                activeGoal = activeGoal
            )
        }
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

    fun onSelectPig(pigId: Long) {
        viewModelScope.launch {
            selectPigUseCase?.invoke(pigId)
        }
    }

    fun onCreatePig(
        name: String,
        targetAmount: Money? = null,
        onSuccess: (Pig) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (createPigUseCase == null) {
                onError("Pig creation service is unavailable")
                return@launch
            }
            createPigUseCase(name = name, targetAmount = targetAmount)
                .onSuccess { pig -> onSuccess(pig) }
                .onFailure { error -> onError(error.message ?: "Failed to create pig") }
        }
    }
}
