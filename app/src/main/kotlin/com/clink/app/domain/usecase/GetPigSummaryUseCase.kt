package com.clink.app.domain.usecase

import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PigSummary(
    val pig: Pig?,
    val recentTransactions: List<Transaction>
)

class GetPigSummaryUseCase(
    private val pigRepository: PigRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(pigId: Long): Flow<PigSummary> {
        return combine(
            pigRepository.getPigById(pigId),
            transactionRepository.getTransactionsForPig(pigId)
        ) { pig, transactions ->
            PigSummary(
                pig = pig,
                recentTransactions = transactions.take(5)
            )
        }
    }
}
