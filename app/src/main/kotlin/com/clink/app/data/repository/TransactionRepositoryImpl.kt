package com.clink.app.data.repository

import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsForPig(pigId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForPig(pigId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun recordTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }
}
