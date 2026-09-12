package com.clink.app.presentation.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.clink.app.domain.repository.PigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * UI representation of a transaction item with pre-formatted date strings
 * and accessible content descriptions to ensure smooth, recomputation-free scrolling.
 */
data class TransactionUiModel(
    val transaction: Transaction,
    val formattedDate: String,
    val accessibilityDescription: String
)

/**
 * State representing the complete History screen.
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val transactions: List<TransactionUiModel> = emptyList(),
    val groupedTransactions: Map<String, List<TransactionUiModel>> = emptyMap(),
    val totalSaved: Money = Money.ZERO,
    val transactionCount: Int = 0,
    val errorMessage: String? = null,
    val pigName: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    savedStateHandle: SavedStateHandle,
    private val pigRepository: PigRepository? = null
) : ViewModel() {

    private val pigId: Long? = savedStateHandle.get<String>("pigId")?.toLongOrNull()
    private val retryTrigger = MutableStateFlow(0)

    private val pigNameFlow: Flow<String?> = if (pigId != null && pigRepository != null) {
        pigRepository.getPigById(pigId).map { it?.name }
    } else {
        flowOf(null)
    }

    val uiState: StateFlow<HistoryUiState> = retryTrigger
        .flatMapLatest {
            combine(
                getTransactionsUseCase(pigId),
                pigNameFlow
            ) { txList, pigName ->
                val total = txList.fold(Money.ZERO) { acc: Money, tx: Transaction -> acc + tx.amount }
                val uiModels = txList.map { tx ->
                    TransactionUiModel(
                        transaction = tx,
                        formattedDate = TransactionDateFormatter.formatTransactionTime(tx.timestamp),
                        accessibilityDescription = TransactionDateFormatter.formatAccessibilityDescription(tx)
                    )
                }
                val grouped = uiModels.groupBy { TransactionDateFormatter.formatDateGroupHeader(it.transaction.timestamp) }
                HistoryUiState(
                    isLoading = false,
                    transactions = uiModels,
                    groupedTransactions = grouped,
                    totalSaved = total,
                    transactionCount = txList.size,
                    errorMessage = null,
                    pigName = pigName
                )
            }.catch { throwable ->
                emit(
                    HistoryUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Couldn't load your savings"
                    )
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    /**
     * Backward-compatible convenience flow of domain transactions.
     */
    val transactions: StateFlow<List<Transaction>> = uiState
        .map { state -> state.transactions.map { it.transaction } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Re-triggers observation flow if an upstream error was encountered.
     */
    fun retry() {
        retryTrigger.value += 1
    }
}
