package com.clink.app.domain.repository

import com.clink.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations for saving history and records.
 */
interface TransactionRepository {
    fun getTransactionsForPig(pigId: Long): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    fun observeTransactions(pigId: Long): Flow<List<Transaction>> = getTransactionsForPig(pigId)
    suspend fun recordTransaction(transaction: Transaction): Long
    suspend fun getTransactionById(id: Long): Transaction?
}
