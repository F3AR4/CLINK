package com.clink.app.data.repository

import androidx.room.withTransaction
import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigRepositoryImpl @Inject constructor(
    private val database: ClinkDatabase,
    private val pigDao: PigDao,
    private val transactionDao: TransactionDao
) : PigRepository {

    override fun getAllPigs(): Flow<List<Pig>> {
        return pigDao.getAllPigs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPigById(id: Long): Flow<Pig?> {
        return pigDao.getPigById(id).map { it?.toDomain() }
    }

    override suspend fun getPigByIdOnce(id: Long): Pig? {
        return pigDao.getPigByIdOnce(id)?.toDomain()
    }

    override suspend fun createPig(pig: Pig): Long {
        return pigDao.insertPig(PigEntity.fromDomain(pig))
    }

    override suspend fun updatePig(pig: Pig) {
        pigDao.updatePig(PigEntity.fromDomain(pig))
    }

    override suspend fun updateBalance(pigId: Long, newBalance: Money) {
        pigDao.updateBalance(id = pigId, balancePaise = newBalance.paise)
    }

    override suspend fun deletePig(pigId: Long) {
        pigDao.deletePig(pigId)
    }

    internal var transactionRunner: suspend (suspend () -> Any?) -> Any? = { block ->
        database.withTransaction { block() }
    }

    /**
     * Atomically increments the pig's balance and records the corresponding transaction
     * within a single Room database transaction.
     */
    override suspend fun addSavings(pigId: Long, amount: Money, note: String): Transaction {
        @Suppress("UNCHECKED_CAST")
        return transactionRunner {
            val pigEntity = pigDao.getPigByIdOnce(pigId)
                ?: throw IllegalArgumentException("Pig with ID $pigId not found")

            val currentBalance = Money(pigEntity.balancePaise)
            val newBalance = currentBalance + amount // Math.addExact prevents overflow

            pigDao.updateBalance(
                id = pigId,
                balancePaise = newBalance.paise,
                updatedAt = System.currentTimeMillis()
            )

            val txEntity = TransactionEntity(
                pigId = pigId,
                amountPaise = amount.paise,
                type = TransactionType.CREDIT.name,
                status = TransactionStatus.COMPLETED.name,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            val txId = transactionDao.insertTransaction(txEntity)
            txEntity.copy(id = txId).toDomain()
        } as Transaction
    }

    /**
     * Retrieves the default/first Pig, or initializes one if none exist.
     * Guarantees idempotent startup without creating duplicate pigs on app launch/restart.
     */
    override suspend fun getOrCreateDefaultPig(): Pig {
        val existing = pigDao.getFirstPig()
        if (existing != null) {
            return existing.toDomain()
        }

        val defaultPig = Pig(
            name = "Primary Pig",
            balance = Money.ZERO,
            targetAmount = Money.fromRupees(5000),
            iconName = "default_pig",
            colorHex = "#E85D75"
        )
        val generatedId = pigDao.insertPig(PigEntity.fromDomain(defaultPig))
        return defaultPig.copy(id = generatedId)
    }
}
