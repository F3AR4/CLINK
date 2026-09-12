package com.clink.app.domain.usecase

import com.clink.app.domain.model.Transaction
import com.clink.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase coordinating transaction retrieval with clean domain boundaries.
 * Supports observing transactions across all pigs or filtered by a specific pigId.
 * Guarantees query results are ordered newest-first (timestamp DESC, id DESC).
 */
class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(pigId: Long? = null): Flow<List<Transaction>> {
        return if (pigId != null) {
            transactionRepository.observeTransactions(pigId)
        } else {
            transactionRepository.getAllTransactions()
        }
    }
}
