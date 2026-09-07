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
}
