package com.clink.app.domain.repository

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining data operations for Piggy banks.
 */
interface PigRepository {
    fun getAllPigs(): Flow<List<Pig>>
    fun getPigById(id: Long): Flow<Pig?>
    suspend fun getPigByIdOnce(id: Long): Pig?
    suspend fun createPig(pig: Pig): Long
    suspend fun updatePig(pig: Pig)
    suspend fun updateBalance(pigId: Long, newBalance: Money)
    suspend fun deletePig(pigId: Long)

    /**
     * Atomically adds savings into the pig, updates the pig balance,
     * and records the credit transaction record.
     */
    suspend fun addSavings(pigId: Long, amount: Money, note: String = "Added savings"): com.clink.app.domain.model.Transaction

    /**
     * Retrieves the primary/default Pig, or creates one if the database is newly initialized.
     */
    suspend fun getOrCreateDefaultPig(): Pig
}
